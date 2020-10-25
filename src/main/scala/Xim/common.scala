package Xim

import chisel3._

class I_imm_bundle extends Bundle {
    val inst_31 = UInt(1.W)
    val inst_30_25 = UInt(6.W)
    val inst_24_21 = UInt(4.W)
    val inst_20 = UInt(1.W)
}

class S_imm_bundle extends Bundle {
    val inst_31 = UInt(1.W)
    val inst_30_25 = UInt(6.W)
    val inst_11_8 = UInt(4.W)
    val inst_7 = UInt(1.W)
}

class B_imm_bundle extends Bundle {
    val inst_31 = UInt(1.W)
    val inst_7 = UInt(1.W)
    val inst_30_25 = UInt(6.W)
    val inst_11_8 = UInt(4.W)
    val zero = UInt(1.W)
}

class U_imm_bundle extends Bundle {
    val inst_31 = UInt(1.W)
    val inst_30_20 = UInt(11.W)
    val inst_19_12 = UInt(8.W)
    val zero = UInt(12.W)
}

class J_imm_bundle extends Bundle {
    val inst_31 = UInt(1.W)
    val inst_19_12 = UInt(8.W)
    val inst_20 = UInt(1.W)
    val inst_30_25 = UInt(6.W)
    val inst_24_21 = UInt(4.W)
    val zero = UInt(1.W)
}

class regfile_raddr extends Bundle {
    val raddr1 = UInt(5.W)
    val raddr2 = UInt(5.W)
}

class decoder_7_128 extends Module {
    val io = IO(new Bundle {
        val in = Input(UInt(7.W))
        val out = Output(UInt(128.W))
    })
    val tmp = Wire(Vec(128, UInt(1.W)))
    for (i <- 0 until 128) {
        when (io.in === i.U) {
            tmp(i) := 1.U
        } .otherwise {
            tmp(i) := 0.U
        }
    }
    io.out := tmp.asUInt
}

class encoder(input_len: Int) extends Module {
    val io = IO(new Bundle{
        val in = Input(input_len.asUInt())
        val out = Output(UInt())
    })
}

class decoder_3_8 extends Module {
    val io = IO(new Bundle {
        val in = Input(UInt(3.W))
        val out = Output(UInt(8.W))
    })
    val tmp = Wire(Vec(8, UInt(1.W)))
    for (i <- 0 until 8) {
        when (io.in === i.U) {
            tmp(i) := 1.U
        } .otherwise {
            tmp(i) := 0.U
        }
    }
    io.out := tmp.asUInt
}

class TLB_entry extends Bundle {
    val page_size = UInt(2.W)
    val VPN2 = UInt(9.W)
    val VPN1 = UInt(9.W)
    val VPN0 = UInt(9.W)
    val PPN2 = UInt(26.W)
    val PPN1 = UInt(9.W)
    val PPN0 = UInt(9.W)
    val D = UInt(1.W)
    val A = UInt(1.W)
    val G = UInt(1.W)
    val U = UInt(1.W)
    val X = UInt(1.W)
    val W = UInt(1.W)
    val R = UInt(1.W)
    val V = UInt(1.W)
}

class VAddr extends Bundle {
    val NotCare = UInt(25.W)
    val VPN2 = UInt(9.W)
    val VPN1 = UInt(9.W)
    val VPN0 = UInt(9.W)
    val offset = UInt(12.W)
}

class PAddr extends Bundle {
    val Zero = UInt(8.W)
    val PPN2 = UInt(26.W)
    val PPN1 = UInt(9.W)
    val PPN0 = UInt(9.W)
    val offset = UInt(12.W)
}

class PTE extends Bundle {
    val NotCare = UInt(10.W)
    val PPN2 = UInt(26.W)
    val PPN1 = UInt(9.W)
    val PPN0 = UInt(9.W)
    val RSW = UInt(2.W)
    val D = UInt(1.W)
    val A = UInt(1.W)
    val G = UInt(1.W)
    val U = UInt(1.W)
    val X = UInt(1.W)
    val W = UInt(1.W)
    val R = UInt(1.W)
    val V = UInt(1.W)
}
