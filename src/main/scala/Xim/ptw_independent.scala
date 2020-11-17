package Xim

/* This is intended for the test of Page Table Walker */

import chisel3._

class ptw_independent(width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val VAddr = Input(UInt(width.W))
        val VAddr_valid = Input(UInt(1.W))
        val TLB_entry_valid = Output(UInt(1.W))
        val TLB_entry = Output(UInt((new TLB_entry).getWidth.W))
    })
    // Provide an AXI RAM for ptw to walk on
    val bridge = Module(new AXI_Bridge(64))
    val ram = Module(new AXI_ram)
    val ptw = Module(new PTW)
    
    ptw.io.req_vaddr := io.VAddr
    ptw.io.req_valid := io.VAddr_valid
    io.TLB_entry_valid := ptw.io.TLB_entry_valid
    io.TLB_entry := ptw.io.TLB_entry
    ptw.io.satp_base := 0x0.U
    
    bridge.io.clock := clock
    bridge.io.reset := reset
    
    bridge.io.inst_req := ptw.io.inst_req
    bridge.io.inst_wr := 0.U
    bridge.io.inst_size := 3.U
    bridge.io.inst_addr := ptw.io.inst_addr
    bridge.io.inst_wdata := 0.U
    ptw.io.inst_data_ok := bridge.io.inst_data_ok
    ptw.io.inst_addr_ok := bridge.io.inst_addr_ok
    ptw.io.inst_rdata := bridge.io.inst_rdata
    
    bridge.io.data_req := 0.U
    bridge.io.data_wr := 0.U
    bridge.io.data_size := 0.U
    bridge.io.data_addr := 0.U
    bridge.io.data_wdata := 0.U
    // we do not care the output of data channel
    
    ram.io.awid := bridge.io.awid
    ram.io.awaddr := bridge.io.awaddr
    ram.io.awlen := bridge.io.awlen
    ram.io.awsize := bridge.io.awsize
    ram.io.awburst := bridge.io.awburst
    ram.io.awlock := bridge.io.awlock
    ram.io.awcache := bridge.io.awcache
    ram.io.awprot := bridge.io.awprot
    ram.io.awvalid := bridge.io.awvalid
    bridge.io.awready := ram.io.awready
    ram.io.wdata := bridge.io.wdata
    ram.io.wstrb := bridge.io.wstrb
    ram.io.wlast := bridge.io.wlast
    ram.io.wvalid := bridge.io.wvalid
    bridge.io.wready := ram.io.wready
    bridge.io.bid := ram.io.bid
    bridge.io.bresp := ram.io.bresp
    bridge.io.bvalid := ram.io.bvalid
    ram.io.bready := bridge.io.bready
    ram.io.arid := bridge.io.arid
    ram.io.araddr := bridge.io.araddr
    ram.io.arlen := bridge.io.arlen
    ram.io.arsize := bridge.io.arsize
    ram.io.arburst := bridge.io.arburst
    ram.io.arlock := bridge.io.arlock
    ram.io.arcache := bridge.io.arcache
    ram.io.arprot := bridge.io.arcache
    ram.io.arvalid := bridge.io.arvalid
    bridge.io.arready := ram.io.arready
    bridge.io.rid := ram.io.rid
    bridge.io.rdata := ram.io.rdata
    bridge.io.rresp := ram.io.rresp
    bridge.io.rlast := ram.io.rlast
    bridge.io.rvalid := ram.io.rvalid
    ram.io.rready := bridge.io.rready
    
    
}
