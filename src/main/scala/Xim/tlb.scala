package Xim

import chisel3._

class tlb extends Module {
    val io = IO(new Bundle {
        val VAddr = Input(64.W)
        val PAddr = Input(64.W)
    })
}
