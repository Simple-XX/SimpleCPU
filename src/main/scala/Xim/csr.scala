package Xim

import chisel3._

class CSR(val rv_width: Int = 64) extends Module {
    val io = IO(new Bundle {
        val es_ex = Input(UInt(1.W))
        val es_new_instr = Input(UInt(1.W))
        val es_excode = Input(UInt(rv_width.W))
        val es_ex_pc = Input(UInt(rv_width.W))
        val es_ex_addr = Input(UInt(rv_width.W))
        val es_csr_wr = Input(UInt(1.W))
        val es_csr_read_num = Input(UInt(12.W))
        val es_csr_write_num = Input(UInt(12.W))
        val es_csr_write_data = Input(UInt(rv_width.W))
        val es_csr_read_data = Output(UInt(rv_width.W))
        // timer interrupt
        val time_int = Output(UInt(1.W))
        // trap entry
        val mtrap_entry = Output(UInt(rv_width.W))
        // MEPC
        val mepc = Output(UInt(rv_width.W))
        
        // exception related
        val mip = Output(UInt(rv_width.W))
        val mie = Output(UInt(rv_width.W))
        val mstatus_mie = Output(UInt(1.W))
        
        // for mtval
        val fault_addr = Input(UInt(rv_width.W))
        val fault_instr = Input(UInt(32.W))
        
        // mret
        val inst_mret = Input(UInt(1.W))
    })
    // unimplemented signal:
    
    object csr_consts extends CSRConstants
    class misa extends Bundle {
        val MXL = UInt(2.W)
        // RV32: 1 RV64: 2
        val WLRL = UInt(36.W)
        val EXTEN = UInt(26.W)
        // I: bit 8 M: bit 12
    }

    class mvendorid extends Bundle {
        val zero = UInt(32.W) // always 32 bit
    }

    class marchid extends Bundle {
        val zero = UInt(rv_width.W)
    }

    class mimpid extends Bundle {
        val zero = UInt(rv_width.W)
    }

    class mhartid extends Bundle {
        val zero = UInt(rv_width.W)
    }

    class mstatus extends Bundle {
        val SD = UInt(1.W) // hardwired to zero
        val reserved = UInt(27.W) // hardwired to zero
        val SXL = UInt(2.W) // RV32:1 RV64:2
        val UXL = UInt(2.W) // RV32:1 RV64:2
        val reserved_1 = UInt(9.W) // hardwired to zero
        val TSR = UInt(1.W) // hardwired to zero
        val TW = UInt(1.W) // hardwired to zero
        val TVM = UInt(1.W) // hardwired to zero
        val MXR = UInt(1.W) // hardwired to zero
        val SUM = UInt(1.W) // hardwired to zero
        val MPRV = UInt(1.W)  // hardwired to zero
        val XS = UInt(2.W) // hardwired to zero
        val FS = UInt(2.W) // hardwired to zero
        val MPP = UInt(2.W) // hardwired to 2'b11
        val reserved_2 = UInt(2.W) // hardwired to zero
        val SPP = UInt(1.W) // hardwired to zero
        val MPIE = UInt(1.W)
        val reserved_3 = UInt(1.W) // hardwired to zero
        val SPIE = UInt(1.W) // hardwired to zero
        val UPIE = UInt(1.W) // hardwired to zero
        val MIE = UInt(1.W)
        val reserved_4 = UInt(1.W) // hardwired to zero
        val SIE = UInt(1.W) // hardwired to zero
        val UIE = UInt(1.W) // hardwired to zero
    }

    class mtvec extends Bundle {
        val base = UInt((rv_width-2).W)
        val mode = UInt(2.W)
    }

    class mip extends Bundle {
        val reserved = UInt(52.W)
        val MEIP = UInt(1.W)
        val reserved_2 = UInt(1.W)
        val SEIP = UInt(1.W)
        val UEIP = UInt(1.W)
        val MTIP = UInt(1.W)
        val reserved_3 = UInt(1.W)
        val STIP = UInt(1.W)
        val UTIP = UInt(1.W)
        val MSIP = UInt(1.W)
        val reserved_4 = UInt(1.W)
        val SSIP = UInt(1.W)
        val USIP = UInt(1.W)
    }

    class mie extends Bundle {
        val reserved = UInt(52.W)
        val MEIE = UInt(1.W)
        val reserved_2 = UInt(1.W)
        val SEIE = UInt(1.W)
        val UEIE = UInt(1.W)
        val MTIE = UInt(1.W)
        val reserved_3 = UInt(1.W)
        val STIE = UInt(1.W)
        val UTIE = UInt(1.W)
        val MSIE = UInt(1.W)
        val reserved_4 = UInt(1.W)
        val SSIE = UInt(1.W)
        val USIE = UInt(1.W)
    }

    // wild chicken implementation here
    class mtime extends Bundle {
        val hi = UInt(32.W)
        val lo = UInt(32.W)
    }

    class mtimecmp extends Bundle {
        val hi = UInt(32.W)
        val lo = UInt(32.W)
    }

    class mscratch extends Bundle {
        val value = UInt(rv_width.W)
    }

    class mepc extends Bundle {
        val value = UInt(rv_width.W)
    }

    class mcause extends Bundle {
        val interrupt = UInt(1.W)
        val excode = UInt((rv_width - 1).W)
    }

    class mtval extends Bundle {
        val value = UInt(rv_width.W)
    }

    // Supervisor Mode CSRs
    class sstatus extends Bundle {

    }
    
    // val es_ex_set = RegInit(0.U(1.W))
    val es_ex_once = Wire(UInt(1.W))
    
    val mret_once = Wire(UInt(1.W))
    // val mret_set = RegInit(0.U(1.W))
    
    val mtime_full = Wire(UInt(64.W))
    val mtime_next_full = Wire(UInt(64.W))
    val mtimecmp_full = Wire(UInt(64.W))
    val time_int = Wire(UInt(1.W))

    val csr_misa = Wire(new misa);
    val csr_mvendorid = Wire(new mvendorid)
    val csr_marchid = Wire(new marchid)
    val csr_mimpid = Wire(new mimpid)
    val csr_mhartid = Wire(new mhartid)
    // MSTATUS
    val reset_mstatus = WireInit(0.U.asTypeOf(new mstatus))
    reset_mstatus.MPP := 0x3.U // Always at machine mode
    val csr_mstatus = RegInit(reset_mstatus)
    // MTVEC
    val reset_mtvec = WireInit(0.U.asTypeOf(new mtvec))
    reset_mtvec.base := 0x10000000.U
    val csr_mtvec = RegInit(reset_mtvec)
    // MIP
    val reset_mip = WireInit(0.U.asTypeOf(new mip))
    reset_mip.MTIP := 0.U
    val csr_mip = RegInit(reset_mip)
    // MIE
    val reset_mie = WireInit(0.U.asTypeOf(new mie))
    val csr_mie = RegInit(reset_mie)
    // MTIME
    val reset_mtime = WireInit(0.U.asTypeOf(new mtime))
    val csr_mtime = RegInit(reset_mtime)
    // MTIMECMP
    val reset_mtimecmp = WireInit(0.U).asTypeOf(new mtimecmp)
    reset_mtimecmp.hi := 0x7000000.U // avoid unexpected TIP
    val csr_mtimecmp = RegInit(reset_mtimecmp)
    // MSCRATCH
    val reset_mscratch = WireInit(0.U.asTypeOf(new mscratch))
    val csr_mscratch = RegInit(reset_mscratch)
    // MEPC
    val reset_mepc = WireInit(0.U.asTypeOf(new mepc))
    val csr_mepc = RegInit(reset_mepc)
    // MCAUSE
    val reset_mcause = WireInit(0.U.asTypeOf(new mcause))
    val csr_mcause = RegInit(reset_mcause)
    // MTVAL
    val reset_mtval = WireInit(0.U.asTypeOf(new mtval))
    val csr_mtval = RegInit(reset_mtval)
    
    when (io.es_new_instr === 1.U && io.es_ex === 1.U) {
        es_ex_once := 1.U
    } .otherwise {
        es_ex_once := 0.U
    }
    
    when (io.es_new_instr === 1.U && io.inst_mret === 1.U) {
        mret_once := 1.U
    } .otherwise {
        mret_once := 0.U
    }

    csr_misa.MXL := 2.U // RV64
    csr_misa.WLRL := 0.U // reserved
    csr_misa.EXTEN := 0x100.U // RV I

    csr_mvendorid.zero := 0.U // reserverd

    csr_marchid.zero := 0.U // reserved

    csr_mimpid.zero := 0.U

    csr_mhartid.zero := 0.U

    // MSTATUS
    when (es_ex_once === 1.U) {
        csr_mstatus.MPIE := csr_mstatus.MIE
    } .elsewhen (mret_once === 1.U) {
        csr_mstatus.MPIE := 1.U // according to the SPEC
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.MPIE := io.es_csr_write_data(7)
    }
    
    when (es_ex_once === 1.U) {
        csr_mstatus.MIE := 0.U
    } .elsewhen (mret_once === 1.U) {
        csr_mstatus.MIE := csr_mstatus.MPIE
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.MIE := io.es_csr_write_data(3)
    }
    
    io.mstatus_mie := csr_mstatus.MIE


    // MTVEC
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTVEC) {
        csr_mtvec.base := io.es_csr_write_data(rv_width - 1, 2)
        // csr_mtvec.mode := io.es_csr_write_data(1,0)
        // DIRECT Mode only
    }
    
    io.mtrap_entry := csr_mtvec.asUInt()

    // MIP
    // TODO: revise updating condition of MIP and MIE
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MIP) {
        // TODO: should be read only here
        csr_mip.MEIP := io.es_csr_write_data(11)
    }
    
    when (csr_mip.MTIP === 1.U && csr_mstatus.MIE === 1.U && csr_mie.MTIE === 1.U) {
        time_int := 1.U
    } .otherwise {
        time_int := 0.U
    }
    
    when (io.es_csr_wr === 1.U && (io.es_csr_write_num === csr_consts.MTIME ||
      io.es_csr_write_num === csr_consts.MTIMECMP )) {
        csr_mip.MTIP := 0.U
    } .elsewhen (mtime_full === mtimecmp_full) {
        csr_mip.MTIP := 1.U
    }
    
    io.mip := csr_mip.asUInt()

    // MIE
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MIE) {
        csr_mie.MEIE := io.es_csr_write_data(11)
        csr_mie.MTIE := io.es_csr_write_data(7)
        csr_mie.MSIE := io.es_csr_write_data(3)
    }
    io.mie := csr_mie.asUInt()

    // MTIME
    // Note that mtime and mtimecmp is memory-mapped, be careful when treating this
    // TODO: memory mapped IO here and for mtimecmp
    io.time_int := time_int
    
    mtime_next_full := csr_mtime.asUInt() + 1.U
    mtime_full := csr_mtime.asUInt()
    mtimecmp_full := csr_mtimecmp.asUInt()
    
    val self_counter = RegInit(0.U(3.W)) // advance every eight cycles
    
    self_counter := self_counter + 1.U
    
    
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTIME) {
        csr_mtime.lo := io.es_csr_write_data(31, 0)
        csr_mtime.hi := io.es_csr_write_data(63, 32)
    } .elsewhen (self_counter === 7.U) {
        csr_mtime.lo := mtime_next_full(31, 0)
        csr_mtime.hi := mtime_next_full(63, 32)
    }

    // MTIMECMP
    // TODO: memory mapped IO
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTIMECMP) {
        csr_mtimecmp.lo := io.es_csr_write_data(31, 0)
        csr_mtimecmp.hi := io.es_csr_write_data(63, 32)
    }


    // MSCRATCH
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSCRATCH) {
        csr_mscratch.value := io.es_csr_write_data
    }

    // MEPC
    when (es_ex_once === 1.U) {
        csr_mepc.value := io.es_ex_pc
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MEPC) {
        csr_mepc.value := io.es_csr_write_data
    }
    
    io.mepc := csr_mepc.asUInt()

    // MCAUSE
    // excode is generated in pipeline
    when (es_ex_once === 1.U) {
        csr_mcause.excode := io.es_excode(rv_width - 2, 0)
        csr_mcause.interrupt := io.es_excode(rv_width - 1)
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MCAUSE) {
        csr_mcause.interrupt := io.es_csr_write_data(rv_width - 1)
        csr_mcause.excode := io.es_csr_write_data(rv_width - 2, 0)
    }

    // MTVAL
    csr_mtval.value := RegInit(0.U)
    when (es_ex_once === 1.U && io.es_excode === excode_const.IllegalInstruction) {
        csr_mtval.value := io.fault_instr
    } .elsewhen (es_ex_once === 1.U && (io.es_excode === excode_const.StoreAddrMisaligned || io.es_excode === excode_const.LoadAddrMisaligned || io.es_excode === excode_const.InstructionMisaligned)) {
        csr_mtval.value := io.fault_addr
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTVAL) {
        csr_mtval.value := io.es_csr_write_data
    }

    // READ Data path

    when (io.es_csr_read_num === csr_consts.MSTATUS) {
        io.es_csr_read_data := csr_mstatus.asUInt()
    } .elsewhen(io.es_csr_read_num === csr_consts.MISA) {
        io.es_csr_read_data := csr_misa.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MVENDORID) {
        io.es_csr_read_data := csr_mvendorid.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MARCHID) {
        io.es_csr_read_data := csr_marchid.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MIMPID) {
        io.es_csr_read_data := csr_mimpid.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MHARTID) {
        io.es_csr_read_data := csr_mhartid.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MTVEC) {
        io.es_csr_read_data := csr_mtvec.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MIP) {
        io.es_csr_read_data := csr_mip.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MIE) {
        io.es_csr_read_data := csr_mie.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MSCRATCH) {
        io.es_csr_read_data := csr_mscratch.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MEPC) {
        io.es_csr_read_data := csr_mepc.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MCAUSE) {
        io.es_csr_read_data := csr_mcause.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MTVAL) {
        io.es_csr_read_data := csr_mtval.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MTIMECMP) {
        io.es_csr_read_data := csr_mtimecmp.asUInt()
    } .elsewhen (io.es_csr_read_num === csr_consts.MTIME) {
        io.es_csr_read_data := csr_mtime.asUInt()
    } .otherwise {
        // WARNING: TEST ONLY
        io.es_csr_read_data := csr_mtime.asUInt()
    }


}

object CSR extends App {
    chisel3.Driver.execute(args, () => new CSR)
}