package Xim

import chisel3._

class AXI_interface extends Bundle {
    private val data_width = 64
    private val addr_width = 64 // 1 Megabyte should be enough for us
    private val wstrb_width = data_width / 8
    private val id_width = 8
    val awid = Input(UInt(data_width.W))
    val awaddr = Input(UInt(addr_width.W))
    val awlen = Input(UInt(8.W))
    val awsize = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
    val awlock = Input(UInt(1.W))
    val awcache = Input(UInt(4.W))
    val awprot = Input(UInt(3.W))
    val awvalid = Input(UInt(1.W))
    val awready = Output(UInt(1.W))
    val awuser = Output(UInt(1.W))
    val awqos = Output(UInt(4.W))
    val wdata = Input(UInt(data_width.W))
    val wstrb = Input(UInt(wstrb_width.W))
    val wlast = Input(UInt(1.W))
    val wvalid = Input(UInt(1.W))
    val wready = Output(UInt(1.W))
    val bid = Output(UInt(id_width.W))
    val bresp = Output(UInt(2.W))
    val bvalid = Output(UInt(1.W))
    val bready = Input(UInt(1.W))
    val buser = Input(UInt(1.W))
    val arid = Input(UInt(id_width.W))
    val araddr = Input(UInt(addr_width.W))
    val arlen = Input(UInt(8.W))
    val arsize = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
    val arlock = Input(UInt(1.W))
    val arcache = Input(UInt(4.W))
    val arprot = Input(UInt(3.W))
    val arvalid = Input(UInt(1.W))
    val arready = Output(UInt(1.W))
    val aruser = Output(UInt(1.W))
    val arqos = Output(UInt(4.W))
    val rid = Output(UInt(id_width.W))
    val rdata = Output(UInt(data_width.W))
    val rresp = Output(UInt(2.W))
    val rlast = Output(UInt(1.W))
    val rvalid = Output(UInt(1.W))
    val rready = Input(UInt(1.W))
    val ruser = Input(UInt(1.W))
}