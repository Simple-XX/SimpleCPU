package Xim

import chisel3._



class SoC extends Module {
    val io = IO(new Bundle {
        val reg_wen = Output(UInt(1.W))
        val reg_wdata = Output(UInt(32.W))
        val reg_waddr = Output(UInt(5.W))
        val es_instr = Output(UInt(32.W))
        val es_pc = Output(UInt(32.W))
        val es_reg_a0 = Output(UInt(32.W))
    })
    
    
    val Core = Module(new CPU_Core)
    val CPU_Bridge = Module(new AXI_Bridge)
    CPU_Bridge.io.clock := clock
    CPU_Bridge.io.reset := reset
    
    val RAM = Module(new AXI_ram)
    RAM.io.clock := clock
    RAM.io.reset := reset
    
    io.reg_wen := Core.io.reg_wen
    io.reg_wdata := Core.io.reg_wdata
    io.reg_waddr := Core.io.reg_waddr
    io.es_pc := Core.io.es_pc
    io.es_instr := Core.io.es_instr
    io.es_reg_a0 := Core.io.es_reg_a0
    
    // CPU_Bridge.io <> RAM.io
    RAM.io.awid := CPU_Bridge.io.awid
    RAM.io.awaddr := CPU_Bridge.io.awaddr
    RAM.io.awlen := CPU_Bridge.io.awlen
    RAM.io.awsize := CPU_Bridge.io.awsize
    RAM.io.awburst := CPU_Bridge.io.awburst
    RAM.io.awlock := CPU_Bridge.io.awlock
    RAM.io.awcache := CPU_Bridge.io.awcache
    RAM.io.awprot := CPU_Bridge.io.awprot
    RAM.io.awvalid := CPU_Bridge.io.awvalid
    CPU_Bridge.io.awready := RAM.io.awready
    RAM.io.wdata := CPU_Bridge.io.wdata
    RAM.io.wstrb := CPU_Bridge.io.wstrb
    RAM.io.wlast := CPU_Bridge.io.wlast
    RAM.io.wvalid := CPU_Bridge.io.wvalid
    CPU_Bridge.io.wready := RAM.io.wready
    CPU_Bridge.io.bid := RAM.io.bid
    CPU_Bridge.io.bresp := RAM.io.bresp
    CPU_Bridge.io.bvalid := RAM.io.bvalid
    RAM.io.bready := CPU_Bridge.io.bready
    RAM.io.arid := CPU_Bridge.io.arid
    RAM.io.araddr := CPU_Bridge.io.araddr
    RAM.io.arlen := CPU_Bridge.io.arlen
    RAM.io.arsize := CPU_Bridge.io.arsize
    RAM.io.arburst := CPU_Bridge.io.arburst
    RAM.io.arlock := CPU_Bridge.io.arlock
    RAM.io.arcache := CPU_Bridge.io.arcache
    RAM.io.arprot := CPU_Bridge.io.arcache
    RAM.io.arvalid := CPU_Bridge.io.arvalid
    CPU_Bridge.io.arready := RAM.io.arready
    CPU_Bridge.io.rid := RAM.io.rid
    CPU_Bridge.io.rdata := RAM.io.rdata
    CPU_Bridge.io.rresp := RAM.io.rresp
    CPU_Bridge.io.rlast := RAM.io.rlast
    CPU_Bridge.io.rvalid := RAM.io.rvalid
    RAM.io.rready := CPU_Bridge.io.rready
    
    CPU_Bridge.io.inst_req := Core.io.inst_req_valid
    CPU_Bridge.io.inst_wr := 0.U
    CPU_Bridge.io.inst_size := 2.U
    CPU_Bridge.io.inst_addr := Core.io.inst_addr
    CPU_Bridge.io.inst_wdata := 0.U
    Core.io.inst_data := CPU_Bridge.io.inst_rdata
    Core.io.inst_req_ack := CPU_Bridge.io.inst_addr_ok
    Core.io.inst_valid := CPU_Bridge.io.inst_data_ok
    
    CPU_Bridge.io.data_req := Core.io.data_write | Core.io.data_read
    CPU_Bridge.io.data_wr := Core.io.data_write
    CPU_Bridge.io.data_size := Core.io.data_size
    CPU_Bridge.io.data_addr := Core.io.data_addr
    CPU_Bridge.io.data_wdata := Core.io.data_write_data
    Core.io.data_read_data := CPU_Bridge.io.data_rdata
    Core.io.data_req_ack := CPU_Bridge.io.data_addr_ok
    Core.io.data_read_valid := CPU_Bridge.io.data_data_ok
    
    val uart = Module(new AXI_fake_serial)
    uart.io.wdata := CPU_Bridge.io.wdata
    uart.io.wvalid := CPU_Bridge.io.wvalid
    uart.io.awaddr := CPU_Bridge.io.awaddr
    uart.io.awvalid := CPU_Bridge.io.awvalid
    
    // printf(p"SRAM-like: inst_addr = ${CPU_Bridge.io.inst_addr} inst_req = ${CPU_Bridge.io.inst_req}, inst_wr = ${CPU_Bridge.io.inst_wr} inst_addr_ok = ${CPU_Bridge.io.inst_addr_ok}\n")
    // printf(p"AXI RAM: araddr = ${CPU_Bridge.io.araddr} arready = ${CPU_Bridge.io.arready} arvalid = ${CPU_Bridge.io.arvalid} rready = ${CPU_Bridge.io.rready} rvalid = ${CPU_Bridge.io.rvalid} rdata = ${CPU_Bridge.io.rdata}\n")
}

object SoC extends App {
    chisel3.Driver.execute(args, () => new SoC)
}