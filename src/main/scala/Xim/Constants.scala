package Xim

import chisel3._

trait CSRConstants {
    val MSTATUS = 0x300.U
    val MISA = 0x301.U
    val MIE = 0x304.U
    val MTVEC = 0x305.U
    val MSCRATCH = 0x340.U
    val MEPC = 0x341.U
    val MCAUSE = 0x342.U
    val MTVAL = 0x343.U
    val MIP = 0x344.U
    val MVENDORID = 0xf11.U
    val MARCHID = 0xf12.U
    val MIMPID = 0xf13.U
    val MHARTID = 0xf14.U
    
    // custom encoding
    val MTIME = 0x7c0.U
    val MTIMECMP = 0x7c2.U

    // Supervisor Mode CSRs
    // Trap Setup
    val SSTATUS = 0x100.U
    val SEDELEG = 0x102.U
    val SIDELEG = 0x103.U
    val SIE = 0x104.U
    val STVEC = 0x105.U
    val SCOUNTEREN = 0x106.U
    // Trap Handling
    val SSCRATCH = 0x140.U
    val SEPC = 0x141.U
    val SCAUSE = 0x142.U
    val STVAL = 0x143.U
    val SIP = 0x144.U
    // Protection and Translation
    // val SATP = 0x180.U
}

trait ExceptionConstants {
    // Currently we do not handle page fault related exceptions
    var MachineTimerInt = UInt(64.W)
    MachineTimerInt = (0x80000007.S).asUInt()
    // No external exceptions for now
    val InstructionMisaligned = 0x00000000.U
    val IllegalInstruction = 0x00000002.U
    val LoadAddrMisaligned = 0x00000004.U
    val StoreAddrMisaligned = 0x00000006.U
    val MEcall = 0x0000000b.U
}

trait PriviledgeLevelConstants {
    val User = 0.U
    val Supervisor = 1.U
    val Machine = 3.U 
}