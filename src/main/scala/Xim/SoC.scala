package Xim

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.stage.RunFirrtlTransformAnnotation



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
    
    io.reg_wen := Core.io.reg_wen
    io.reg_wdata := Core.io.reg_wdata
    io.reg_waddr := Core.io.reg_waddr
    io.es_pc := Core.io.es_pc
    io.es_instr := Core.io.es_instr
    io.es_reg_a0 := Core.io.es_reg_a0
    
    val RAM = Module(new AXI_ram)
    RAM.io.clock := clock
    RAM.io.reset := reset
    
    val MMIO = Module(new AXI_ram)
    MMIO.io.clock := clock
    MMIO.io.reset := reset
    
    RAM.io.awid := Core.io.axi_mem.awid
    RAM.io.awaddr := Core.io.axi_mem.awaddr
    RAM.io.awlen := Core.io.axi_mem.awlen
    RAM.io.awsize := Core.io.axi_mem.awsize
    RAM.io.awburst := Core.io.axi_mem.awburst
    RAM.io.awlock := Core.io.axi_mem.awlock
    RAM.io.awcache := Core.io.axi_mem.awcache
    RAM.io.awprot := Core.io.axi_mem.awprot
    RAM.io.awvalid := Core.io.axi_mem.awvalid
    Core.io.axi_mem.awready := RAM.io.awready
    Core.io.axi_mem.awqos := 0.U
    Core.io.axi_mem.awuser := 0.U
    RAM.io.wdata := Core.io.axi_mem.wdata
    RAM.io.wstrb := Core.io.axi_mem.wstrb
    RAM.io.wlast := Core.io.axi_mem.wlast
    RAM.io.wvalid := Core.io.axi_mem.wvalid
    Core.io.axi_mem.wready := RAM.io.wready
    Core.io.axi_mem.bid := RAM.io.bid
    Core.io.axi_mem.bresp := RAM.io.bresp
    Core.io.axi_mem.bvalid := RAM.io.bvalid
    RAM.io.bready := Core.io.axi_mem.bready
    RAM.io.arid := Core.io.axi_mem.arid
    RAM.io.araddr := Core.io.axi_mem.araddr
    RAM.io.arlen := Core.io.axi_mem.arlen
    RAM.io.arsize := Core.io.axi_mem.arsize
    RAM.io.arburst := Core.io.axi_mem.arburst
    RAM.io.arlock := Core.io.axi_mem.arlock
    RAM.io.arcache := Core.io.axi_mem.arcache
    RAM.io.arprot := Core.io.axi_mem.arcache
    RAM.io.arvalid := Core.io.axi_mem.arvalid
    Core.io.axi_mem.arready := RAM.io.arready
    Core.io.axi_mem.arqos := 0.U
    Core.io.axi_mem.aruser := 0.U
    Core.io.axi_mem.rid := RAM.io.rid
    Core.io.axi_mem.rdata := RAM.io.rdata
    Core.io.axi_mem.rresp := RAM.io.rresp
    Core.io.axi_mem.rlast := RAM.io.rlast
    Core.io.axi_mem.rvalid := RAM.io.rvalid
    RAM.io.rready := Core.io.axi_mem.rready
    // Core.io.axi_mem.ruser := 0.U
    
    MMIO.io.awid := Core.io.axi_mmio.awid
    MMIO.io.awaddr := Core.io.axi_mmio.awaddr
    MMIO.io.awlen := Core.io.axi_mmio.awlen
    MMIO.io.awsize := Core.io.axi_mmio.awsize
    MMIO.io.awburst := Core.io.axi_mmio.awburst
    MMIO.io.awlock := Core.io.axi_mmio.awlock
    MMIO.io.awcache := Core.io.axi_mmio.awcache
    MMIO.io.awprot := Core.io.axi_mmio.awprot
    MMIO.io.awvalid := Core.io.axi_mmio.awvalid
    Core.io.axi_mmio.awqos := 0.U
    Core.io.axi_mmio.awuser := 0.U
    Core.io.axi_mmio.awready := MMIO.io.awready
    MMIO.io.wdata := Core.io.axi_mmio.wdata
    MMIO.io.wstrb := Core.io.axi_mmio.wstrb
    MMIO.io.wlast := Core.io.axi_mmio.wlast
    MMIO.io.wvalid := Core.io.axi_mmio.wvalid
    Core.io.axi_mmio.wready := MMIO.io.wready
    Core.io.axi_mmio.bid := MMIO.io.bid
    Core.io.axi_mmio.bresp := MMIO.io.bresp
    Core.io.axi_mmio.bvalid := MMIO.io.bvalid
    MMIO.io.bready := Core.io.axi_mmio.bready
    MMIO.io.arid := Core.io.axi_mmio.arid
    MMIO.io.araddr := Core.io.axi_mmio.araddr
    MMIO.io.arlen := Core.io.axi_mmio.arlen
    MMIO.io.arsize := Core.io.axi_mmio.arsize
    MMIO.io.arburst := Core.io.axi_mmio.arburst
    MMIO.io.arlock := Core.io.axi_mmio.arlock
    MMIO.io.arcache := Core.io.axi_mmio.arcache
    MMIO.io.arprot := Core.io.axi_mmio.arcache
    MMIO.io.arvalid := Core.io.axi_mmio.arvalid
    Core.io.axi_mmio.arqos := 0.U
    Core.io.axi_mmio.aruser := 0.U
    Core.io.axi_mmio.arready := MMIO.io.arready
    Core.io.axi_mmio.rid := MMIO.io.rid
    Core.io.axi_mmio.rdata := MMIO.io.rdata
    Core.io.axi_mmio.rresp := MMIO.io.rresp
    Core.io.axi_mmio.rlast := MMIO.io.rlast
    Core.io.axi_mmio.rvalid := MMIO.io.rvalid
    MMIO.io.rready := Core.io.axi_mmio.rready

    
    val uart = Module(new AXI_fake_serial)
    uart.io.wdata := Core.io.axi_mem.wdata
    uart.io.wvalid := Core.io.axi_mem.wvalid
    uart.io.awaddr := Core.io.axi_mem.awaddr
    uart.io.awvalid := Core.io.axi_mem.awvalid
    
    // printf(p"SRAM-like: inst_addr = ${Core.io.inst_addr} inst_req = ${Core.io.inst_req}, inst_wr = ${Core.io.inst_wr} inst_addr_ok = ${Core.io.inst_addr_ok}\n")
    // printf(p"AXI RAM: araddr = ${Core.io.araddr} arready = ${Core.io.arready} arvalid = ${Core.io.arvalid} rready = ${Core.io.rready} rvalid = ${Core.io.rvalid} rdata = ${Core.io.rdata}\n")
}

object SoC extends App {
    (new ChiselStage).execute(
        args,
        Seq(
            ChiselGeneratorAnnotation(() => new SoC()),
            RunFirrtlTransformAnnotation(new AddModulePrefix()),
            ModulePrefixAnnotation("chenguokai_")
        )
    )
    
    //chisel3.Driver.execute(args, () => new CPU_Core_SoC())
}