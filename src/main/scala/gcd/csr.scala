package Xim

import chisel3._

class misa extends Bundle {
    val MXL = UInt(2.W)
    // RV32: 1 RV64: 2
    val WLRL = UInt(4.W)
    val EXTEN = UInt(26.W)
    // I: bit 8 M: bit 12
}

class mvendorid extends Bundle {
    val zero = UInt(32.W)
}

class marchid extends Bundle {
    val zero = UInt(32.W)
}

class mimpid extends Bundle {
    val zero = UInt(32.W)
}

class mhartid extends Bundle {
    val zero = UInt(32.W)
}

class mstatus extends Bundle {
    val SD = UInt(1.W) // hardwired to zero
    val reserved = UInt(8.W) // hardwired to zero
    val TSR = UInt(1.W) // hardwired to zero
    val TW = UInt(1.W) // hardwired to zero
    val TVM = UInt(1.W) // hardwired to zero
    val MXR = UInt(1.W) // hardwired to zero
    val SUM = UInt(1.W) // hardwired to zero
    val MPRV = UInt(1.W)  // hardwired to zero
    val XS = UInt(2.W) // hardwired to zero
    val FS = UInt(2.W) // hardwired to zero
    val MPP = UInt(2.W)
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
    val base = UInt(30.W)
    val mode = UInt(2.W)
}

class mip extends Bundle {
    val reserved = UInt(20.W)
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
    val reserved = UInt(20.W)
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
    val zero = UInt(32.W)
    val value = UInt(32.W)
}

class mtimecmp extends Bundle {
    val zero = UInt(32.W)
    val value = UInt(32.W)
}

class mscratch extends Bundle {
    val value = UInt(32.W)
}

class mepc extends Bundle {
    val value = UInt(32.W)
}

class mcause extends Bundle {
    val interrupt = UInt(1.W)
    val excode = UInt(31.W)
}

class mtval extends Bundle {
    val value = UInt(32.W)
}

class CSR extends Module {
    val io = IO(new Bundle {
        val es_ex = Input(UInt(1.W))
        val es_excode = Input(UInt(32.W))
        val es_ex_pc = Input(UInt(32.W))
        val es_ex_addr = Input(UInt(32.W))
        val es_csr_wr = Input(UInt(1.W))
        val es_csr_read_num = Input(UInt(12.W))
        val es_csr_write_num = Input(UInt(12.W))
        val es_csr_write_data = Input(UInt(32.W))

    })
}