package Xim

import chisel3._
import chisel3.iotesters.PeekPokeTester

class SoC_UnitTester(c: SoC) extends PeekPokeTester(c) {
    // private val SoC = c
    
    var i:Int = 0
    
    while (i < 1000000 && peek(c.io.es_instr) != BigInt(0x73)) {
        step(1)
        i += 1
    }
    expect(c.io.es_reg_a0, 0) // expect a0 to be zero
    /*
    for (i <- 0 to 1000) {
        step(1)
    }
    
     */
}