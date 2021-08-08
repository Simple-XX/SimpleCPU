package Xim

import chisel3._

class PriviledgeReg extends Module {
    val io = IO(new Bundle {
        val wen = Input(Bool())
        val wlevel = Input(UInt(2.W))
        val rlevel = Output(UInt(2.W))
    })
    
    val priviledge_level = RegInit(3.U(2.W)) //initialized as Machine level
    
    when(io.wen) {
        priviledge_level := io.wlevel
    }

    io.rlevel := priviledge_level
}