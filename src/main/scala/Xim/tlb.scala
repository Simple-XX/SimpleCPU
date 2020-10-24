package Xim

import chisel3._

class tlb extends Module {
    val io = IO(new Bundle {
        val VAddr = Input(UInt(64.W))
        val PAddr = Output(UInt(64.W))
    })
}
