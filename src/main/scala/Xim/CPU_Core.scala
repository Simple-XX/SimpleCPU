
package Xim

import chisel3._

class CPU_Core(val rv_width: Int = 64) extends Module {
    val io = IO(Flipped(new AXI_interface {
        // for debug purpose, flipped
        val reg_wen          = Input(UInt(1.W))
        val reg_wdata        = Input(UInt(rv_width.W))
        val reg_waddr        = Input(UInt(5.W))
        val es_instr         = Input(UInt(32.W))
        val es_pc            = Input(UInt(rv_width.W))
        val es_reg_a0        = Input(UInt(rv_width.W))
    }))
    val inst_addr        = Wire(UInt(rv_width.W))
    val inst_req_valid   = Wire(UInt(1.W))
    val inst_req_ack     = Wire(UInt(1.W))
    
    val inst_data        = Wire(UInt(rv_width.W))
    val inst_valid       = Wire(UInt(1.W))
    val inst_ack         = Wire(UInt(1.W))
    
    val data_addr        = Wire(UInt(rv_width.W))
    val data_write       = Wire(UInt(1.W))
    val data_read        = Wire(UInt(1.W))
    val data_size        = Wire(UInt(2.W))
    
    val data_write_data  = Wire(UInt(rv_width.W))
    
    val data_req_ack      = Wire(UInt(1.W))
    
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
    data_write := EX_Stage.io.data_write
    data_read := EX_Stage.io.data_read
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
    
    CPU_Bridge.io.inst_req := inst_req_valid
    CPU_Bridge.io.inst_wr := 0.U
    CPU_Bridge.io.inst_size := 2.U
    CPU_Bridge.io.inst_addr := inst_addr
    CPU_Bridge.io.inst_wdata := 0.U
    inst_data := CPU_Bridge.io.inst_rdata
    inst_req_ack := CPU_Bridge.io.inst_addr_ok
    inst_valid := CPU_Bridge.io.inst_data_ok
    
    CPU_Bridge.io.data_req := data_write | data_read
    CPU_Bridge.io.data_wr := data_write
    CPU_Bridge.io.data_size := data_size
    CPU_Bridge.io.data_addr := data_addr
    CPU_Bridge.io.data_wdata := data_write_data
    data_read_data := CPU_Bridge.io.data_rdata
    data_req_ack := CPU_Bridge.io.data_addr_ok
    data_read_valid := CPU_Bridge.io.data_data_ok
    
    io.awid := CPU_Bridge.io.awid
    io.awaddr := CPU_Bridge.io.awaddr
    io.awlen := CPU_Bridge.io.awlen
    io.awsize := CPU_Bridge.io.awsize
    io.awburst := CPU_Bridge.io.awburst
    io.awlock := CPU_Bridge.io.awlock
    io.awcache := CPU_Bridge.io.awcache
    io.awprot := CPU_Bridge.io.awprot
    io.awvalid := CPU_Bridge.io.awvalid
    CPU_Bridge.io.awready := io.awready
    io.wdata := CPU_Bridge.io.wdata
    io.wstrb := CPU_Bridge.io.wstrb
    io.wlast := CPU_Bridge.io.wlast
    io.wvalid := CPU_Bridge.io.wvalid
    CPU_Bridge.io.wready := io.wready
    CPU_Bridge.io.bid := io.bid
    CPU_Bridge.io.bresp := io.bresp
    CPU_Bridge.io.bvalid := io.bvalid
    io.bready := CPU_Bridge.io.bready
    io.arid := CPU_Bridge.io.arid
    io.araddr := CPU_Bridge.io.araddr
    io.arlen := CPU_Bridge.io.arlen
    io.arsize := CPU_Bridge.io.arsize
    io.arburst := CPU_Bridge.io.arburst
    io.arlock := CPU_Bridge.io.arlock
    io.arcache := CPU_Bridge.io.arcache
    io.arprot := CPU_Bridge.io.arcache
    io.arvalid := CPU_Bridge.io.arvalid
    CPU_Bridge.io.arready := io.arready
    CPU_Bridge.io.rid := io.rid
    CPU_Bridge.io.rdata := io.rdata
    CPU_Bridge.io.rresp := io.rresp
    CPU_Bridge.io.rlast := io.rlast
    CPU_Bridge.io.rvalid := io.rvalid
    io.rready := CPU_Bridge.io.rready
}

object CPU_Core extends App {
    chisel3.Driver.execute(args, () => new CPU_Core)
}