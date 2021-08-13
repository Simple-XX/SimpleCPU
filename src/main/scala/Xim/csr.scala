package Xim

import chisel3._

class CSR(val rv_width: Int = 64) extends Module {
    val io = IO(new Bundle {
        val es_ex = Input(UInt(1.W))
        val es_ex_work = Output(UInt(1.W))
        val es_new_instr = Input(UInt(1.W))
        val es_excode = Input(UInt(rv_width.W))
        val es_ex_pc = Input(UInt(rv_width.W))
        val es_ex_addr = Input(UInt(rv_width.W))
        val es_csr_wr = Input(UInt(1.W))
        val es_csr_op = Input(UInt(1.W))
        val es_csr_read_num = Input(UInt(12.W))
        val es_csr_write_num = Input(UInt(12.W))
        val es_csr_write_data = Input(UInt(rv_width.W))
        val es_csr_read_data = Output(UInt(rv_width.W))
        // timer interrupt
        val time_int = Output(UInt(1.W))
        // trap entry
        val trap_entry = Output(UInt(rv_width.W))
        // MEPC
        val mepc = Output(UInt(rv_width.W))
        
        // exception related
        val mstatus_tsr = Output(UInt(1.W))
        val mstatus_mpp = Output(UInt(2.W))

        // Supervisor related
        val sstatus_spp = Output(UInt(1.W))
        val sepc = Output(UInt(rv_width.W))

        // for mtval
        val fault_addr = Input(UInt(rv_width.W))
        val fault_instr = Input(UInt(32.W))
        
        // mret
        val inst_mret = Input(UInt(1.W))
        val mret_work = Output(UInt(1.W))
        // sret
        val inst_sret = Input(UInt(1.W))
        val sret_work = Output(UInt(1.W))

        // priviledge level
        val priv_level = Input(UInt(2.W))

        // reload signal, avoiding illegal state transition
        val inst_reload = Input(UInt(1.W))

        // delegate trap
        val deleg_trap = Output(Bool())

        // illegal csr operations
        val illegal_csr = Output(Bool())
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
        val TSR = UInt(1.W)
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

    class medeleg extends Bundle {
        val value = UInt(rv_width.W)
    }

    class mideleg extends Bundle {
        val value = UInt(rv_width.W)
    }

    // Supervisor Mode CSRs
    class sstatus extends Bundle {
        val SD = UInt(1.W)
        val reserved = UInt(29.W)
        val UXL = UInt(2.W)
        val reserved_1 = UInt(12.W)
        val MXR = UInt(1.W)   //currently not used because we do not support virtual memory
        val SUM = UInt(1.W)   //currently not used because we do not support virtual memory
        val reserved_2 = UInt(1.W)
        val XS = UInt(2.W)
        val FS = UInt(2.W)
        val reserved_3 = UInt(4.W)
        val SPP = UInt(1.W)
        val reserved_4 = UInt(2.W)
        val SPIE = UInt(1.W)
        val UPIE = UInt(1.W)
        val reserved_5 = UInt(2.W)
        val SIE = UInt(1.W)
        val UIE = UInt(1.W)
    }

    class sepc extends Bundle {
        val value = UInt(rv_width.W)
    }

    class scause extends Bundle {
        val interrupt = UInt(1.W)
        val excode = UInt((rv_width - 1).W)
    }

    class stvec extends Bundle {
        val base = UInt((rv_width-2).W)
        val mode = UInt(2.W)
    }

    class sie extends Bundle {
        val reserved = UInt(54.W)
        val SEIE = UInt(1.W)
        val UEIE = UInt(1.W)
        val reserved_1 = UInt(2.W)
        val STIE = UInt(1.W)
        val UTIE = UInt(1.W)
        val reserved_2 = UInt(2.W)
        val SSIE = UInt(1.W)
        val USIE = UInt(1.W)
    }

    class sip extends Bundle {
        val reserved = UInt(54.W)
        val SEIP = UInt(1.W)
        val UEIP = UInt(1.W)
        val reserved_1 = UInt(2.W)
        val STIP = UInt(1.W)
        val UTIP = UInt(1.W)
        val reserved_2 = UInt(2.W)
        val SSIP = UInt(1.W)
        val USIP = UInt(1.W)
    }

    class stval extends Bundle {
        val value = UInt(rv_width.W)
    }

    class sscratch extends Bundle {
        val value = UInt(rv_width.W)
    }
    
    // val es_ex_set = RegInit(0.U(1.W))
    val es_ex_once = Wire(UInt(1.W))
    
    val mret_once = Wire(UInt(1.W))
    val sret_once = Wire(UInt(1.W))
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
    reset_mstatus.SXL := 0x2.U // RV64
    reset_mstatus.UXL := 0x2.U // RV64
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
    // MEDELEG
    val reset_medeleg = WireInit(0.U.asTypeOf(new medeleg))
    //reset_medeleg.value(11) := 0.U  // m-mode ecall exception hardwired to zero
    val csr_medeleg = RegInit(reset_medeleg)
    // MIDELEG
    val reset_mideleg = WireInit(0.U.asTypeOf(new mideleg))
    val csr_mideleg = RegInit(reset_mideleg)

    // SSTATUS
    val reset_sstatus = WireInit(0.U.asTypeOf(new sstatus))
    //...
    val csr_sstatus = RegInit(reset_sstatus)
    // SEPC
    val reset_sepc = WireInit(0.U.asTypeOf(new sepc))
    val csr_sepc = RegInit(reset_sepc)

    // STVEC
    val reset_stvec = WireInit(0.U.asTypeOf(new stvec))
    reset_stvec.base := 0x10000000.U
    val csr_stvec = RegInit(reset_stvec)
    // SIP
    val reset_sip = WireInit(0.U.asTypeOf(new sip))
    reset_sip.STIP := 0.U
    val csr_sip = RegInit(reset_sip)
    // SIE
    val reset_sie = WireInit(0.U.asTypeOf(new sie))
    val csr_sie = RegInit(reset_sie)
    // SSCRATCH
    val reset_sscratch = WireInit(0.U.asTypeOf(new sscratch))
    val csr_sscratch = RegInit(reset_sscratch)
    // SCAUSE
    val reset_scause = WireInit(0.U.asTypeOf(new scause))
    val csr_scause = RegInit(reset_scause)
    // STVAL
    val reset_stval = WireInit(0.U.asTypeOf(new stval))
    val csr_stval = RegInit(reset_stval)

    val isInterrupt = Wire(Bool())
    val deleg = Wire(Bool())
    val SupervisorTIDeleg = Wire(UInt(1.W))
    val UserTIDeleg = Wire(UInt(1.W))
    val machine_csr_op = Wire(Bool())

    isInterrupt := io.es_excode(rv_width - 1) === 1.U
    deleg := ((isInterrupt && csr_mideleg.value(io.es_excode(3,0)) =/= 0.U) || (!isInterrupt && csr_medeleg.value(io.es_excode(3,0)) =/= 0.U)) && io.priv_level =/= priv_consts.Machine
    io.deleg_trap := deleg
    SupervisorTIDeleg := csr_mideleg.value(5)
    UserTIDeleg := csr_mideleg.value(4)
    
    when (io.es_new_instr === 1.U && io.es_ex === 1.U) {
        es_ex_once := 1.U
    } .otherwise {
        es_ex_once := 0.U
    }
    
    when (io.es_new_instr === 1.U && io.inst_mret === 1.U && io.inst_reload === 0.U) {
        mret_once := 1.U
    } .otherwise {
        mret_once := 0.U
    }

    when (io.es_new_instr === 1.U && io.inst_sret === 1.U && io.inst_reload === 0.U) {
        sret_once := 1.U
    } .otherwise {
        sret_once := 0.U
    }

    csr_misa.MXL := 2.U // RV64
    csr_misa.WLRL := 0.U // reserved
    csr_misa.EXTEN := 0x100.U // RV I

    csr_mvendorid.zero := 0.U // reserverd

    csr_marchid.zero := 0.U // reserved

    csr_mimpid.zero := 0.U

    csr_mhartid.zero := 0.U

    io.es_ex_work := es_ex_once
    io.mret_work := mret_once
    io.sret_work := sret_once

    // timer interrupt
    when (mtime_full === mtimecmp_full) {
        when (io.priv_level === priv_consts.Machine) {
            csr_mip.MTIP := 1.U
        } .elsewhen (io.priv_level === priv_consts.Supervisor) {
            when (SupervisorTIDeleg === 1.U) {
                csr_sip.STIP := 1.U
            } .otherwise {
                csr_mip.MTIP := 1.U
            }
        } .otherwise {
            when (UserTIDeleg === 1.U) {
                csr_sip.STIP := 1.U
            } .otherwise {
                csr_mip.MTIP := 1.U
            }
        }
    }

    when (csr_mip.MTIP === 1.U && csr_mstatus.MIE === 1.U && csr_mie.MTIE === 1.U) {
        time_int := 1.U
    } .elsewhen (csr_sip.STIP === 1.U && csr_sstatus.SIE === 1.U && csr_sie.STIE === 1.U) {
        time_int := 1.U
    } .otherwise {
        time_int := 0.U
    }

    // MSTATUS
    when (es_ex_once === 1.U && !deleg) {
        csr_mstatus.MPIE := csr_mstatus.MIE
    } .elsewhen (mret_once === 1.U) {
        csr_mstatus.MPIE := 1.U // according to the SPEC
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.MPIE := io.es_csr_write_data(7)
    }
    
    when (es_ex_once === 1.U && !deleg) {
        csr_mstatus.MIE := 0.U
    } .elsewhen (mret_once === 1.U) {
        csr_mstatus.MIE := csr_mstatus.MPIE
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.MIE := io.es_csr_write_data(3)
    }

    when (es_ex_once === 1.U && !deleg) {
        csr_mstatus.MPP := io.priv_level
    } .elsewhen (mret_once === 1.U) {
        csr_mstatus.MPP := priv_consts.User // by spec
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.MPP := io.es_csr_write_data(12,11)
    }

    when (es_ex_once === 1.U && deleg) {
        csr_mstatus.SPP := io.priv_level
        csr_mstatus.SPIE := csr_mstatus.SIE
        csr_mstatus.SIE := 0.U
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MSTATUS) {
        csr_mstatus.SPP := io.es_csr_write_data(8)
        csr_mstatus.SPIE := io.es_csr_write_data(5)
        csr_mstatus.SIE := io.es_csr_write_data(1)
    }
    
    //io.mstatus_mie := csr_mstatus.MIE
    io.mstatus_tsr := csr_mstatus.TSR
    io.mstatus_mpp := csr_mstatus.MPP
    io.sstatus_spp := csr_sstatus.SPP

    // MTVEC
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTVEC) {
        csr_mtvec.base := io.es_csr_write_data(rv_width - 1, 2)
        // csr_mtvec.mode := io.es_csr_write_data(1,0)
        // DIRECT Mode only
    }
    
    when (deleg) {
        io.trap_entry := csr_stvec.asUInt()
    } .otherwise {
        io.trap_entry := csr_mtvec.asUInt()
    }

    // MIP
    // TODO: revise updating condition of MIP and MIE
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MIP) {
        // TODO: should be read only here
        csr_mip.MEIP := io.es_csr_write_data(11)
    }
    
    when (io.es_csr_wr === 1.U && (io.es_csr_write_num === csr_consts.MTIME ||
      io.es_csr_write_num === csr_consts.MTIMECMP )) {
        csr_mip.MTIP := 0.U
    }
    
    //io.mip := csr_mip.asUInt()

    // MIE
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MIE) {
        csr_mie.MEIE := io.es_csr_write_data(11)
        csr_mie.MTIE := io.es_csr_write_data(7)
        csr_mie.MSIE := io.es_csr_write_data(3)
    }
    //io.mie := csr_mie.asUInt()

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
    when (es_ex_once === 1.U && !deleg) {
        csr_mepc.value := io.es_ex_pc
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MEPC) {
        csr_mepc.value := io.es_csr_write_data
    }
    
    io.mepc := csr_mepc.asUInt()
    io.sepc := csr_sepc.asUInt()

    // MCAUSE
    // excode is generated in pipeline
    when (es_ex_once === 1.U && !deleg) {
        csr_mcause.excode := io.es_excode(rv_width - 2, 0)
        csr_mcause.interrupt := io.es_excode(rv_width - 1)
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MCAUSE) {
        csr_mcause.interrupt := io.es_csr_write_data(rv_width - 1)
        csr_mcause.excode := io.es_csr_write_data(rv_width - 2, 0)
    }

    // MTVAL
    csr_mtval.value := RegInit(0.U)
    when (es_ex_once === 1.U && io.es_excode === excode_const.IllegalInstruction && !deleg) {
        csr_mtval.value := io.fault_instr
    } .elsewhen (es_ex_once === 1.U && (io.es_excode === excode_const.StoreAddrMisaligned || io.es_excode === excode_const.LoadAddrMisaligned || io.es_excode === excode_const.InstructionMisaligned) && !deleg) {
        csr_mtval.value := io.fault_addr
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MTVAL) {
        csr_mtval.value := io.es_csr_write_data
    }

    // MIDELEG
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MIDELEG) {
        csr_mideleg.value := io.es_csr_write_data
    }

    // MEDELEG
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.MEDELEG) {
        csr_medeleg.value := io.es_csr_write_data
    }

    // SSTATUS
    when (es_ex_once === 1.U && deleg) {
        csr_sstatus.SPIE := csr_sstatus.SIE
    } .elsewhen (sret_once === 1.U) {
        csr_sstatus.SPIE := 1.U // according to the SPEC
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SSTATUS) {
        csr_sstatus.SPIE := io.es_csr_write_data(5)
    }
    
    when (es_ex_once === 1.U && deleg) {
        csr_sstatus.SIE := 0.U
    } .elsewhen (sret_once === 1.U) {
        csr_sstatus.SIE := csr_sstatus.SPIE
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SSTATUS) {
        csr_sstatus.SIE := io.es_csr_write_data(1)
    }

    when (es_ex_once === 1.U && deleg) {
        csr_sstatus.SPP := io.priv_level
    } .elsewhen (sret_once === 1.U) {
        csr_sstatus.SPP := 0.U // User, by spec
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SSTATUS) {
        csr_sstatus.SPP := io.es_csr_write_data(8)
    }

    // SEPC
    when (es_ex_once === 1.U && deleg) {
        csr_sepc.value := io.es_ex_pc
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SEPC) {
        csr_sepc.value := io.es_csr_write_data
    }

    // STVEC
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.STVEC) {
        csr_stvec.base := io.es_csr_write_data(rv_width - 1, 2)
        // csr_mtvec.mode := io.es_csr_write_data(1,0)
        // DIRECT Mode only
    }

    // SIP
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SIP) {
        csr_sip.SEIP := io.es_csr_write_data(9)
    }
    
    when (io.es_csr_wr === 1.U && (io.es_csr_write_num === csr_consts.MTIME ||
      io.es_csr_write_num === csr_consts.MTIMECMP )) {
        csr_sip.STIP := 0.U
    }
    
    // SIE
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SIE) {
        csr_sie.SEIE := io.es_csr_write_data(9)
        csr_sie.STIE := io.es_csr_write_data(5)
        csr_sie.SSIE := io.es_csr_write_data(1)
    }

    // SSCRATCH
    when (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SSCRATCH) {
        csr_sscratch.value := io.es_csr_write_data
    }

    // SCAUSE
    // excode is generated in pipeline
    when (es_ex_once === 1.U && deleg) {
        csr_scause.excode := io.es_excode(rv_width - 2, 0)
        csr_scause.interrupt := io.es_excode(rv_width - 1)
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.SCAUSE) {
        csr_scause.interrupt := io.es_csr_write_data(rv_width - 1)
        csr_scause.excode := io.es_csr_write_data(rv_width - 2, 0)
    }

    // STVAL
    csr_stval.value := RegInit(0.U)
    when (es_ex_once === 1.U && io.es_excode === excode_const.IllegalInstruction && deleg) {
        csr_stval.value := io.fault_instr
    } .elsewhen (es_ex_once === 1.U && (io.es_excode === excode_const.StoreAddrMisaligned || io.es_excode === excode_const.LoadAddrMisaligned || io.es_excode === excode_const.InstructionMisaligned) && deleg) {
        csr_stval.value := io.fault_addr
    } .elsewhen (io.es_csr_wr === 1.U && io.es_csr_write_num === csr_consts.STVAL) {
        csr_stval.value := io.es_csr_write_data
    }

    // READ Data path

    when (io.es_csr_read_num === csr_consts.MSTATUS) {
        io.es_csr_read_data := csr_mstatus.asUInt()
        machine_csr_op := true.B
    } .elsewhen(io.es_csr_read_num === csr_consts.MISA) {
        io.es_csr_read_data := csr_misa.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MVENDORID) {
        io.es_csr_read_data := csr_mvendorid.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MARCHID) {
        io.es_csr_read_data := csr_marchid.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MIMPID) {
        io.es_csr_read_data := csr_mimpid.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MHARTID) {
        io.es_csr_read_data := csr_mhartid.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MTVEC) {
        io.es_csr_read_data := csr_mtvec.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MIP) {
        io.es_csr_read_data := csr_mip.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MIE) {
        io.es_csr_read_data := csr_mie.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MSCRATCH) {
        io.es_csr_read_data := csr_mscratch.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MEPC) {
        io.es_csr_read_data := csr_mepc.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MCAUSE) {
        io.es_csr_read_data := csr_mcause.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MTVAL) {
        io.es_csr_read_data := csr_mtval.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MIDELEG) {
        io.es_csr_read_data := csr_mideleg.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MEDELEG) {
        io.es_csr_read_data := csr_medeleg.asUInt()
        machine_csr_op := true.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MTIMECMP) {
        io.es_csr_read_data := csr_mtimecmp.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.MTIME) {
        io.es_csr_read_data := csr_mtime.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SSTATUS) {
        io.es_csr_read_data := csr_sstatus.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.STVEC) {
        io.es_csr_read_data := csr_stvec.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SEPC) {
        io.es_csr_read_data := csr_sepc.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SIP) {
        io.es_csr_read_data := csr_sip.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SIE) {
        io.es_csr_read_data := csr_sie.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SSCRATCH) {
        io.es_csr_read_data := csr_sscratch.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.SCAUSE) {
        io.es_csr_read_data := csr_scause.asUInt()
        machine_csr_op := false.B
    } .elsewhen (io.es_csr_read_num === csr_consts.STVAL) {
        io.es_csr_read_data := csr_stval.asUInt()
        machine_csr_op := false.B
    } .otherwise {
        // WARNING: TEST ONLY
        io.es_csr_read_data := csr_mtime.asUInt()
        machine_csr_op := false.B
    }
    
    when (io.priv_level =/= priv_consts.Machine && io.es_csr_op === 1.U && machine_csr_op) {
        io.illegal_csr := true.B
    } .otherwise {
        io.illegal_csr := false.B
    }
}

object CSR extends App {
    chisel3.Driver.execute(args, () => new CSR)
}

class CSRSignal(val rv_width: Int = 64) extends Module {
    val io = IO(new Bundle {
        val mepc = Output(UInt(rv_width.W))
        val sepc = Output(UInt(rv_width.W))

        val es_new_instr = Input(UInt(1.W))
        val es_ex = Input(UInt(1.W))
        val es_ex_work = Output(UInt(1.W))
        val es_excode = Input(UInt(rv_width.W))
        val es_pc = Input(UInt(rv_width.W))
        val es_instr = Input(UInt(32.W))
        val es_csr = Input(UInt(1.W))

        val inst_reserved = Input(UInt(1.W))
        val inst_reload_r = Input(UInt(1.W))
        val inst_mret = Input(UInt(1.W))
        val inst_sret = Input(UInt(1.W))
        val mret_work = Output(UInt(1.W))
        val sret_work = Output(UInt(1.W))
        val data_addr = Input(UInt(rv_width.W))

        val Csr_num = Input(UInt(12.W))
        val csr_read_data = Output(UInt(rv_width.W))
        val csr_write_data = Input(UInt(rv_width.W))
        val trap_entry = Output(UInt(rv_width.W))

        val timer_int = Output(UInt(1.W))
        val priv_level = Input(UInt(2.W))

        val mstatus_tsr = Output(UInt(1.W))
        val mstatus_mpp = Output(UInt(2.W))
        val sstatus_spp = Output(UInt(1.W))

        val deleg_trap = Output(Bool())

        val illegal_csr = Output(Bool())
    })

    val CSR_module = Module(new CSR)
    val CSR_ex = Wire(UInt(1.W))
    val CSR_excode = Wire(UInt(rv_width.W))
    val CSR_epc = Wire(UInt(rv_width.W))
    val CSR_badaddr = Wire(UInt(rv_width.W))
    val CSR_write = Wire(UInt(1.W))
    val CSR_read_num = Wire(UInt(12.W))
    val CSR_write_num = Wire(UInt(12.W))
    val CSR_fault_addr = Wire(UInt(rv_width.W))
    val CSR_fault_instr = Wire(UInt(rv_width.W))
    val CSR_op = Wire(UInt(1.W))

    CSR_ex := io.es_ex
    CSR_excode := io.es_excode
    CSR_epc := io.es_pc

    CSR_module.io.es_ex := CSR_ex
    CSR_module.io.es_new_instr := io.es_new_instr // this is high only when new instr finally come in
    CSR_module.io.es_excode := CSR_excode
    CSR_module.io.es_ex_pc := CSR_epc
    CSR_module.io.es_ex_addr := CSR_badaddr
    CSR_module.io.es_csr_wr := CSR_write
    CSR_module.io.es_csr_op := CSR_op
    CSR_module.io.es_csr_read_num := CSR_read_num
    CSR_module.io.es_csr_write_num := CSR_write_num
    CSR_module.io.es_csr_write_data := io.csr_write_data
    CSR_module.io.fault_addr := CSR_fault_addr
    CSR_module.io.fault_instr := CSR_fault_instr
    CSR_module.io.inst_mret := io.inst_mret
    CSR_module.io.inst_sret := io.inst_sret
    CSR_module.io.priv_level := io.priv_level
    CSR_module.io.inst_reload := io.inst_reload_r
    io.csr_read_data := CSR_module.io.es_csr_read_data
    io.trap_entry := CSR_module.io.trap_entry
    io.mstatus_tsr := CSR_module.io.mstatus_tsr
    io.mstatus_mpp := CSR_module.io.mstatus_mpp
    io.sstatus_spp := CSR_module.io.sstatus_spp
    io.mepc := CSR_module.io.mepc
    io.sepc := CSR_module.io.sepc
    io.timer_int := CSR_module.io.time_int
    io.deleg_trap := CSR_module.io.deleg_trap
    io.illegal_csr := CSR_module.io.illegal_csr
    io.es_ex_work := CSR_module.io.es_ex_work
    io.mret_work := CSR_module.io.mret_work
    io.sret_work := CSR_module.io.sret_work

    when (io.inst_reserved === 1.U) {
        // fill with instruction itself
        // TODO: deal with unaligned related exceptions
        CSR_badaddr := io.es_instr
    } .elsewhen (io.es_excode === excode_const.InstructionMisaligned) {
        CSR_badaddr := io.es_pc
    } .elsewhen (io.es_excode === excode_const.LoadAddrMisaligned || io.es_excode === excode_const.StoreAddrMisaligned) {
        CSR_badaddr := io.data_addr
    } .otherwise {
        CSR_badaddr := 0.U
    }

    when (io.es_excode === excode_const.InstructionMisaligned) {
        CSR_fault_addr := io.es_pc
    } .elsewhen (io.es_excode === excode_const.LoadAddrMisaligned || io.es_excode === excode_const.StoreAddrMisaligned) {
        CSR_fault_addr := io.data_addr
    } .otherwise {
        CSR_fault_addr := 0.U
    }

    when (io.es_csr === 1.U && io.es_new_instr === 1.U && io.es_ex === 0.U) {
        CSR_write := 1.U
    } .otherwise {
        CSR_write := 0.U
    }

    when (io.es_csr === 1.U) {
        CSR_op := 1.U
    } .otherwise {
        CSR_op := 0.U
    }

    CSR_fault_instr := io.es_instr
    CSR_read_num := io.Csr_num
    CSR_write_num := io.Csr_num
}