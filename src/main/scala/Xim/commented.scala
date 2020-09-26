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


