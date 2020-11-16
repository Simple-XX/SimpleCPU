
package Xim

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.stage.RunFirrtlTransformAnnotation

class CPU_Core_SoC(val rv_width: Int = 64, inSOC: Boolean = false) extends Module {
    val io = IO(new Bundle {
        val meip = Input(UInt(1.W))
        val mem = Flipped(new AXI_interface)
        val mmio = Flipped(new AXI_lite_interface)
    })
    val inst_addr        = Wire(UInt(rv_width.W))
    val inst_req_valid   = Wire(UInt(1.W))
    val inst_req_ack     = Wire(UInt(1.W))
    
    val inst_data        = Wire(UInt(rv_width.W))
    val inst_valid       = Wire(UInt(1.W))
    val inst_ack         = Wire(UInt(1.W))
    
    val data_addr        = Wire(UInt(rv_width.W))
    val data_write_mem   = Wire(UInt(1.W))
    val data_read_mem    = Wire(UInt(1.W))
    val data_write_mmio  = Wire(UInt(1.W))
    val data_read_mmio   = Wire(UInt(1.W))
    val data_size        = Wire(UInt(2.W))
    
    val data_write_data  = Wire(UInt(rv_width.W))
    
    val data_req_ack     = Wire(UInt(1.W))
    
    val data_read_data   = Wire(UInt(rv_width.W))
    val data_read_valid  = Wire(UInt(1.W))
    val data_data_ack    = Wire(UInt(1.W))
    
    val IF_Stage = Module(new CPU_IF(rv_width))
    inst_addr := IF_Stage.io.inst_addr
    inst_req_valid := IF_Stage.io.inst_req_valid
    IF_Stage.io.inst_req_ack := inst_req_ack
    IF_Stage.io.inst_data := inst_data
    IF_Stage.io.inst_valid := inst_valid
    inst_ack := IF_Stage.io.inst_ack
    
    val EX_Stage = Module(new CPU_EX(rv_width))
    data_addr := EX_Stage.io.data_addr
    data_write_mem := EX_Stage.io.data_write_mem
    data_read_mem := EX_Stage.io.data_read_mem
    data_write_mmio := EX_Stage.io.data_write_mmio
    data_read_mmio := EX_Stage.io.data_read_mmio
    data_size := EX_Stage.io.data_size
    data_write_data := EX_Stage.io.data_write_data
    EX_Stage.io.data_req_ack := data_req_ack
    EX_Stage.io.data_read_data := data_read_data
    EX_Stage.io.data_read_valid := data_read_valid
    data_data_ack := EX_Stage.io.data_data_ack

    
    EX_Stage.io.fs_pc := IF_Stage.io.fs_pc
    EX_Stage.io.fs_inst := IF_Stage.io.fs_inst
    EX_Stage.io.fs_ex := IF_Stage.io.fs_ex
    EX_Stage.io.fs_excode := IF_Stage.io.fs_excode
    EX_Stage.io.fs_to_es_valid := IF_Stage.io.fs_to_es_valid
    EX_Stage.io.es_next_branch := IF_Stage.io.es_next_branch
    
    IF_Stage.io.inst_reload := EX_Stage.io.inst_reload
    IF_Stage.io.es_allowin := EX_Stage.io.es_allowin
    IF_Stage.io.br_valid := EX_Stage.io.br_valid
    IF_Stage.io.br_target := EX_Stage.io.br_target
    IF_Stage.io.ex_valid := EX_Stage.io.ex_valid
    IF_Stage.io.ex_target := EX_Stage.io.ex_target
    
    val branch_predicter = Module(new branch_pred)
    
    IF_Stage.io.next_branch := branch_predicter.io.IF_next_branch
    branch_predicter.io.EX_new_instr := EX_Stage.io.branch_new_instr
    branch_predicter.io.EX_br_taken := EX_Stage.io.branch_br_taken
    
    val CPU_Bridge = Module(new AXI_Bridge(64))
    CPU_Bridge.io.clock := clock
    CPU_Bridge.io.reset := reset
    
    CPU_Bridge.io.inst_req := inst_req_valid & ~IF_Stage.io.inst_req_mmio
    CPU_Bridge.io.inst_wr := 0.U
    CPU_Bridge.io.inst_size := 2.U
    CPU_Bridge.io.inst_addr := inst_addr
    CPU_Bridge.io.inst_wdata := 0.U
    
    CPU_Bridge.io.data_req := data_write_mem | data_read_mem
    CPU_Bridge.io.data_wr := data_write_mem
    CPU_Bridge.io.data_size := data_size
    CPU_Bridge.io.data_addr := data_addr
    CPU_Bridge.io.data_wdata := data_write_data
    
    
    val MMIO_Bridge = Module(new AXI_Bridge(64))
    
    MMIO_Bridge.io.clock := clock
    MMIO_Bridge.io.reset := reset
    
    MMIO_Bridge.io.inst_req := inst_req_valid & IF_Stage.io.inst_req_mmio // do not send any request with inst for now
    MMIO_Bridge.io.inst_wr := 0.U
    MMIO_Bridge.io.inst_size := 2.U
    MMIO_Bridge.io.inst_addr := inst_addr
    MMIO_Bridge.io.inst_wdata := 0.U
    inst_req_ack := (IF_Stage.io.inst_req_mmio & MMIO_Bridge.io.inst_addr_ok) | (~IF_Stage.io.inst_req_mmio & CPU_Bridge.io.inst_addr_ok)
    inst_valid := (IF_Stage.io.inst_req_mmio & MMIO_Bridge.io.inst_data_ok) | (~IF_Stage.io.inst_req_mmio & CPU_Bridge.io.inst_data_ok)
    
    MMIO_Bridge.io.data_req := data_write_mmio | data_read_mmio
    MMIO_Bridge.io.data_wr := data_write_mmio
    MMIO_Bridge.io.data_size := data_size
    MMIO_Bridge.io.data_addr := data_addr
    MMIO_Bridge.io.data_wdata := data_write_data
    
    when (EX_Stage.io.is_mmio === 1.U) {
        data_read_data := MMIO_Bridge.io.data_rdata
    } .otherwise {
        data_read_data := CPU_Bridge.io.data_rdata
    }
    
    when (IF_Stage.io.inst_req_mmio === 1.U) {
        inst_data := MMIO_Bridge.io.inst_rdata
    } .otherwise {
        inst_data := CPU_Bridge.io.inst_rdata
    }
    
    data_req_ack := (EX_Stage.io.is_mmio & MMIO_Bridge.io.data_addr_ok) | (~EX_Stage.io.is_mmio & CPU_Bridge.io.data_addr_ok)
    data_read_valid := (EX_Stage.io.is_mmio & MMIO_Bridge.io.data_data_ok) | (~EX_Stage.io.is_mmio & CPU_Bridge.io.data_data_ok)
    
    io.mem.awid := CPU_Bridge.io.awid
    io.mem.awaddr := CPU_Bridge.io.awaddr
    io.mem.awlen := CPU_Bridge.io.awlen
    io.mem.awsize := CPU_Bridge.io.awsize
    io.mem.awburst := CPU_Bridge.io.awburst
    io.mem.awlock := CPU_Bridge.io.awlock
    io.mem.awcache := CPU_Bridge.io.awcache
    io.mem.awprot := CPU_Bridge.io.awprot
    io.mem.awvalid := CPU_Bridge.io.awvalid
    io.mem.ruser := 0.U
    CPU_Bridge.io.awready := io.mem.awready
    io.mem.wdata := CPU_Bridge.io.wdata
    io.mem.wstrb := CPU_Bridge.io.wstrb
    io.mem.wlast := CPU_Bridge.io.wlast
    io.mem.wvalid := CPU_Bridge.io.wvalid
    CPU_Bridge.io.wready := io.mem.wready
    CPU_Bridge.io.bid := io.mem.bid
    CPU_Bridge.io.bresp := io.mem.bresp
    CPU_Bridge.io.bvalid := io.mem.bvalid
    io.mem.bready := CPU_Bridge.io.bready
    io.mem.buser := 0.U
    io.mem.arid := CPU_Bridge.io.arid
    io.mem.araddr := CPU_Bridge.io.araddr
    io.mem.arlen := CPU_Bridge.io.arlen
    io.mem.arsize := CPU_Bridge.io.arsize
    io.mem.arburst := CPU_Bridge.io.arburst
    io.mem.arlock := CPU_Bridge.io.arlock
    io.mem.arcache := CPU_Bridge.io.arcache
    io.mem.arprot := CPU_Bridge.io.arcache
    io.mem.arvalid := CPU_Bridge.io.arvalid
    CPU_Bridge.io.arready := io.mem.arready
    CPU_Bridge.io.rid := io.mem.rid
    CPU_Bridge.io.rdata := io.mem.rdata
    CPU_Bridge.io.rresp := io.mem.rresp
    CPU_Bridge.io.rlast := io.mem.rlast
    CPU_Bridge.io.rvalid := io.mem.rvalid
    io.mem.rready := CPU_Bridge.io.rready
    
    io.mmio.awaddr := MMIO_Bridge.io.awaddr
    io.mmio.awprot := MMIO_Bridge.io.awprot
    io.mmio.awvalid := MMIO_Bridge.io.awvalid
    MMIO_Bridge.io.awready := io.mmio.awready
    io.mmio.wdata := MMIO_Bridge.io.wdata
    io.mmio.wstrb := MMIO_Bridge.io.wstrb
    io.mmio.wvalid := MMIO_Bridge.io.wvalid
    MMIO_Bridge.io.wready := io.mmio.wready
    MMIO_Bridge.io.bresp := io.mmio.bresp
    MMIO_Bridge.io.bvalid := io.mmio.bvalid
    io.mmio.bready := MMIO_Bridge.io.bready
    io.mmio.araddr := MMIO_Bridge.io.araddr
    io.mmio.arprot := MMIO_Bridge.io.arcache
    io.mmio.arvalid := MMIO_Bridge.io.arvalid
    MMIO_Bridge.io.arready := io.mmio.arready
    MMIO_Bridge.io.rdata := io.mmio.rdata
    MMIO_Bridge.io.rresp := io.mmio.rresp
    MMIO_Bridge.io.rvalid := io.mmio.rvalid
    io.mmio.rready := MMIO_Bridge.io.rready
    
}

object CPU_Core_SoC extends App {
    (new ChiselStage).execute(
        args,
        Seq(
            ChiselGeneratorAnnotation(() => new CPU_Core_SoC()),
            RunFirrtlTransformAnnotation(new AddModulePrefix()),
            ModulePrefixAnnotation("chenguokai_")
        )
    )
    
    //chisel3.Driver.execute(args, () => new CPU_Core_SoC())
}