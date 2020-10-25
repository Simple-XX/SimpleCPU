package Xim

import chisel3._

class RegFile (readPorts: Int) extends Module{
 val io = IO(new Bundle{
   val wen = Input(Bool())
   val waddr = Input(UInt(5.W))
   val wdata = Input(UInt(32.W))
   val raddr = Input(Vec(readPorts, UInt(5.W)))
   val rdata = Output(Vec(readPorts, UInt(32.W)))
     val debug_a0 = Output(UInt(32.W))
 })

 val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

 when(io.wen & io.waddr =/= 0.U){
   regs(io.waddr) := io.wdata
 }

 for(i<-0 until readPorts){
   io.rdata(i) := regs(io.raddr(i))
 }
    
    io.debug_a0 := regs(10) // a0 is for debug purpose

}