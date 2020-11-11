package Xim

// Note that this file stores some reference code which is no longer used in actual code

/*
*     when (inst_sw === 1.U) {
        io.data_wstrb := 0xf.U
    } .elsewhen (inst_sh === 1.U) {
        io.data_wstrb := 0x3.U
    } .elsewhen (inst_sb === 1.U) {
        io.data_wstrb := 0x1.U
    } .otherwise {
        io.data_wstrb := 0.U;
    }
* */

/*
reg_raddr.raddr1 := reg_raddr_1
reg_raddr.raddr2 := reg_raddr_2
 */

/*
    csr_mstatus.SD := 0.U // hardwired to zero
    csr_mstatus.reserved := 0.U // hardwired to zero
    csr_mstatus.TSR := 0.U // hardwired to zero
    csr_mstatus.TW := 0.U // hardwired to zero
    csr_mstatus.TVM := 0.U // hardwired to zero
    csr_mstatus.MXR := 0.U // hardwired to zero
    csr_mstatus.SUM := 0.U // hardwired to zero
    csr_mstatus.MPRV := 0.U // hardwired to zero
    csr_mstatus.XS := 0.U // hardwired to zero
    csr_mstatus.FS := 0.U // hardwired to zero
    csr_mstatus.MPP := 0x3.U // always machine mode
    csr_mstatus.reserved_2 := 0.U // hardwired to zero
    csr_mstatus.SPP := 0.U // hardwired to zero

    csr_mstatus.MPIE := RegInit(0.U);
    csr_mstatus.MIE := RegInit(0.U);
     */
/*
    csr_mstatus.reserved_3 := 0.U // hardwired to zero
    csr_mstatus.SPIE := 0.U // hardwired to zero
    csr_mstatus.UPIE := 0.U // hardwired to zero
    csr_mstatus.reserved_4 := 0.U // hardwired to zero
    csr_mstatus.SIE := 0.U // hardwired to zero
    csr_mstatus.UIE := 0.U // hardwired to zero
     */


/*
csr_mtvec.base := RegInit(0.U)
csr_mtvec.mode := RegInit(0.U)
 */


/*
csr_mip.reserved := 0.U // hardwired to zero
csr_mip.reserved_2 := 0.U // hardwired to zero
csr_mip.SEIP := 0.U // hardwired to zero
csr_mip.UEIP := 0.U // hardwired to zero
csr_mip.MTIP := 1.U // hardwired to one
csr_mip.reserved_3 := 0.U // hardwired to zero
csr_mip.STIP := 0.U // hardwired to zero
csr_mip.UTIP := 0.U // hardwired to zero
csr_mip.MSIP := 0.U // hardwired to zero
csr_mip.reserved_4 := 0.U // hardwired to zero
csr_mip.SSIP := 0.U // hardwired to zero
csr_mip.USIP := 0.U // hardwired to zero

csr_mip.MEIP := RegInit(0.U)
 */


/*
csr_mie.reserved := 0.U // hardwired to zero
csr_mie.reserved_2 := 0.U // hardwired to zero
csr_mie.SEIE := 0.U // hardwired to zero
csr_mie.UEIE := 0.U // hardwired to zero
csr_mie.reserved_3 := 0.U // hardwired to zero
csr_mie.STIE := 0.U // hardwired to zero
csr_mie.UTIE := 0.U // hardwired to zero
csr_mie.reserved_4 := 0.U // hardwired to zero
csr_mie.SSIE := 0.U // hardwired to zero
csr_mie.USIE := 0.U // hardwired to zero

csr_mie.MSIE := RegInit(0.U)
csr_mie.MTIE := RegInit(0.U)
csr_mie.MEIE := RegInit(0.U)
 */


/*
csr_mtime.zero := 0.U
csr_mtime.value := RegInit(0.U)
 */


/*
csr_mtimecmp.zero := 0.U
csr_mtimecmp.value := RegInit(0.U)
 */


/*
csr_mscratch.value := RegInit(0.U)
 */


/*
csr_mepc.value := RegInit(0.U)
 */

/*
// PTW_Bridge.io <> io.AXI part
    io.awid := PTW_Bridge.io.awid
    io.awaddr := PTW_Bridge.io.awaddr
    io.awlen := PTW_Bridge.io.awlen
    io.awsize := PTW_Bridge.io.awsize
    io.awburst := PTW_Bridge.io.awburst
    io.awlock := PTW_Bridge.io.awlock
    io.awcache := PTW_Bridge.io.awcache
    io.awprot := PTW_Bridge.io.awprot
    io.awvalid := PTW_Bridge.io.awvalid
    PTW_Bridge.io.awready := io.awready
    io.wdata := PTW_Bridge.io.wdata
    io.wstrb := PTW_Bridge.io.wstrb
    io.wlast := PTW_Bridge.io.wlast
    io.wvalid := PTW_Bridge.io.wvalid
    PTW_Bridge.io.wready := io.wready
    PTW_Bridge.io.bid := io.bid
    PTW_Bridge.io.bresp := io.bresp
    PTW_Bridge.io.bvalid := io.bvalid
    io.bready := PTW_Bridge.io.bready
    io.arid := PTW_Bridge.io.arid
    io.araddr := PTW_Bridge.io.araddr
    io.arlen := PTW_Bridge.io.arlen
    io.arsize := PTW_Bridge.io.arsize
    io.arburst := PTW_Bridge.io.arburst
    io.arlock := PTW_Bridge.io.arlock
    io.arcache := PTW_Bridge.io.arcache
    io.arprot := PTW_Bridge.io.arcache
    io.arvalid := PTW_Bridge.io.arvalid
    PTW_Bridge.io.arready := io.arready
    PTW_Bridge.io.rid := io.rid
    PTW_Bridge.io.rdata := io.rdata
    PTW_Bridge.io.rresp := io.rresp
    PTW_Bridge.io.rlast := io.rlast
    PTW_Bridge.io.rvalid := io.rvalid
    io.rready := PTW_Bridge.io.rready
* */

/*
    // we do not use SRAM-like interface data channel
    PTW_Bridge.io.data_req := 0.U
    PTW_Bridge.io.data_wr := 0.U
    PTW_Bridge.io.data_size := 0.U
    PTW_Bridge.io.data_addr := 0.U
    PTW_Bridge.io.data_wdata := 0.U
* */