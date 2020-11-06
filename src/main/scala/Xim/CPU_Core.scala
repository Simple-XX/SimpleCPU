
package Xim

import chisel3._

class CPU_Core(val rv_width: Int = 64, inSOC: Boolean = false) extends Module {
    val io = IO(new Bundle {
        val axi_mem = Flipped(new AXI_interface)
        val axi_mmio = Flipped(new AXI_interface)
        // for debug purpose
        val reg_wen          = Output(UInt(1.W))
        val reg_wdata        = Output(UInt(rv_width.W))
        val reg_waddr        = Output(UInt(5.W))
        val es_instr         = Output(UInt(32.W))
        val es_pc            = Output(UInt(rv_width.W))
        val es_reg_a0        = Output(UInt(rv_width.W))
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
    
    // debug part
    io.reg_waddr := EX_Stage.io.es_reg_waddr
    io.reg_wdata := EX_Stage.io.es_reg_wdata
    io.reg_wen := EX_Stage.io.es_reg_wen
    io.es_instr := EX_Stage.io.es_instr
    io.es_pc := EX_Stage.io.es_pc
    io.es_reg_a0 := EX_Stage.io.es_reg_a0
    
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
    
    io.axi_mem.awid := CPU_Bridge.io.awid
    io.axi_mem.awaddr := CPU_Bridge.io.awaddr
    io.axi_mem.awlen := CPU_Bridge.io.awlen
    io.axi_mem.awsize := CPU_Bridge.io.awsize
    io.axi_mem.awburst := CPU_Bridge.io.awburst
    io.axi_mem.awlock := CPU_Bridge.io.awlock
    io.axi_mem.awcache := CPU_Bridge.io.awcache
    io.axi_mem.awprot := CPU_Bridge.io.awprot
    io.axi_mem.awvalid := CPU_Bridge.io.awvalid
    CPU_Bridge.io.awready := io.axi_mem.awready
    io.axi_mem.wdata := CPU_Bridge.io.wdata
    io.axi_mem.wstrb := CPU_Bridge.io.wstrb
    io.axi_mem.wlast := CPU_Bridge.io.wlast
    io.axi_mem.wvalid := CPU_Bridge.io.wvalid
    CPU_Bridge.io.wready := io.axi_mem.wready
    CPU_Bridge.io.bid := io.axi_mem.bid
    CPU_Bridge.io.bresp := io.axi_mem.bresp
    CPU_Bridge.io.bvalid := io.axi_mem.bvalid
    io.axi_mem.bready := CPU_Bridge.io.bready
    io.axi_mem.arid := CPU_Bridge.io.arid
    io.axi_mem.araddr := CPU_Bridge.io.araddr
    io.axi_mem.arlen := CPU_Bridge.io.arlen
    io.axi_mem.arsize := CPU_Bridge.io.arsize
    io.axi_mem.arburst := CPU_Bridge.io.arburst
    io.axi_mem.arlock := CPU_Bridge.io.arlock
    io.axi_mem.arcache := CPU_Bridge.io.arcache
    io.axi_mem.arprot := CPU_Bridge.io.arcache
    io.axi_mem.arvalid := CPU_Bridge.io.arvalid
    CPU_Bridge.io.arready := io.axi_mem.arready
    CPU_Bridge.io.rid := io.axi_mem.rid
    CPU_Bridge.io.rdata := io.axi_mem.rdata
    CPU_Bridge.io.rresp := io.axi_mem.rresp
    CPU_Bridge.io.rlast := io.axi_mem.rlast
    CPU_Bridge.io.rvalid := io.axi_mem.rvalid
    io.axi_mem.rready := CPU_Bridge.io.rready
    
    io.axi_mmio.awid := MMIO_Bridge.io.awid
    io.axi_mmio.awaddr := MMIO_Bridge.io.awaddr
    io.axi_mmio.awlen := MMIO_Bridge.io.awlen
    io.axi_mmio.awsize := MMIO_Bridge.io.awsize
    io.axi_mmio.awburst := MMIO_Bridge.io.awburst
    io.axi_mmio.awlock := MMIO_Bridge.io.awlock
    io.axi_mmio.awcache := MMIO_Bridge.io.awcache
    io.axi_mmio.awprot := MMIO_Bridge.io.awprot
    io.axi_mmio.awvalid := MMIO_Bridge.io.awvalid
    MMIO_Bridge.io.awready := io.axi_mmio.awready
    io.axi_mmio.wdata := MMIO_Bridge.io.wdata
    io.axi_mmio.wstrb := MMIO_Bridge.io.wstrb
    io.axi_mmio.wlast := MMIO_Bridge.io.wlast
    io.axi_mmio.wvalid := MMIO_Bridge.io.wvalid
    MMIO_Bridge.io.wready := io.axi_mmio.wready
    MMIO_Bridge.io.bid := io.axi_mmio.bid
    MMIO_Bridge.io.bresp := io.axi_mmio.bresp
    MMIO_Bridge.io.bvalid := io.axi_mmio.bvalid
    io.axi_mmio.bready := MMIO_Bridge.io.bready
    io.axi_mmio.arid := MMIO_Bridge.io.arid
    io.axi_mmio.araddr := MMIO_Bridge.io.araddr
    io.axi_mmio.arlen := MMIO_Bridge.io.arlen
    io.axi_mmio.arsize := MMIO_Bridge.io.arsize
    io.axi_mmio.arburst := MMIO_Bridge.io.arburst
    io.axi_mmio.arlock := MMIO_Bridge.io.arlock
    io.axi_mmio.arcache := MMIO_Bridge.io.arcache
    io.axi_mmio.arprot := MMIO_Bridge.io.arcache
    io.axi_mmio.arvalid := MMIO_Bridge.io.arvalid
    MMIO_Bridge.io.arready := io.axi_mmio.arready
    MMIO_Bridge.io.rid := io.axi_mmio.rid
    MMIO_Bridge.io.rdata := io.axi_mmio.rdata
    MMIO_Bridge.io.rresp := io.axi_mmio.rresp
    MMIO_Bridge.io.rlast := io.axi_mmio.rlast
    MMIO_Bridge.io.rvalid := io.axi_mmio.rvalid
    io.axi_mmio.rready := MMIO_Bridge.io.rready
    
}

object CPU_Core extends App {
    chisel3.Driver.execute(args, () => new CPU_Core)
}