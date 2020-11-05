package Xim

import chisel3._



class SoC(val rv_width: Int = 64) extends Module {
    val io = IO(new Bundle {
        val reg_wen = Output(UInt(1.W))
        val reg_wdata = Output(UInt(rv_width.W))
        val reg_waddr = Output(UInt(5.W))
        val es_instr = Output(UInt(32.W))
        val es_pc = Output(UInt(rv_width.W))
        val es_reg_a0 = Output(UInt(rv_width.W))
    })
    
    
    val Core = Module(new CPU_Core(64))
    
    val RAM = Module(new AXI_ram)
    RAM.io.clock := clock
    RAM.io.reset := reset
    
    io.reg_wen := Core.io.reg_wen
    io.reg_wdata := Core.io.reg_wdata
    io.reg_waddr := Core.io.reg_waddr
    io.es_pc := Core.io.es_pc
    io.es_instr := Core.io.es_instr
    io.es_reg_a0 := Core.io.es_reg_a0
    
    // Core.io <> RAM.io
    RAM.io.awid := Core.io.awid
    RAM.io.awaddr := Core.io.awaddr
    RAM.io.awlen := Core.io.awlen
    RAM.io.awsize := Core.io.awsize
    RAM.io.awburst := Core.io.awburst
    RAM.io.awlock := Core.io.awlock
    RAM.io.awcache := Core.io.awcache
    RAM.io.awprot := Core.io.awprot
    RAM.io.awvalid := Core.io.awvalid
    Core.io.awready := RAM.io.awready
    RAM.io.wdata := Core.io.wdata
    RAM.io.wstrb := Core.io.wstrb
    RAM.io.wlast := Core.io.wlast
    RAM.io.wvalid := Core.io.wvalid
    Core.io.wready := RAM.io.wready
    Core.io.bid := RAM.io.bid
    Core.io.bresp := RAM.io.bresp
    Core.io.bvalid := RAM.io.bvalid
    RAM.io.bready := Core.io.bready
    RAM.io.arid := Core.io.arid
    RAM.io.araddr := Core.io.araddr
    RAM.io.arlen := Core.io.arlen
    RAM.io.arsize := Core.io.arsize
    RAM.io.arburst := Core.io.arburst
    RAM.io.arlock := Core.io.arlock
    RAM.io.arcache := Core.io.arcache
    RAM.io.arprot := Core.io.arcache
    RAM.io.arvalid := Core.io.arvalid
    Core.io.arready := RAM.io.arready
    Core.io.rid := RAM.io.rid
    Core.io.rdata := RAM.io.rdata
    Core.io.rresp := RAM.io.rresp
    Core.io.rlast := RAM.io.rlast
    Core.io.rvalid := RAM.io.rvalid
    RAM.io.rready := Core.io.rready
    

    
    val uart = Module(new AXI_fake_serial)
    uart.io.wdata := Core.io.wdata
    uart.io.wvalid := Core.io.wvalid
    uart.io.awaddr := Core.io.awaddr
    uart.io.awvalid := Core.io.awvalid
    
    // printf(p"SRAM-like: inst_addr = ${Core.io.inst_addr} inst_req = ${Core.io.inst_req}, inst_wr = ${Core.io.inst_wr} inst_addr_ok = ${Core.io.inst_addr_ok}\n")
    // printf(p"AXI RAM: araddr = ${Core.io.araddr} arready = ${Core.io.arready} arvalid = ${Core.io.arvalid} rready = ${Core.io.rready} rvalid = ${Core.io.rvalid} rdata = ${Core.io.rdata}\n")
}

object SoC extends App {
    chisel3.Driver.execute(args, () => new SoC)
}