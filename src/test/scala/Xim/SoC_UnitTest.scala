package Xim

import chisel3._
import chisel3.iotesters.PeekPokeTester

class SoC_UnitTester(c: SoC) extends PeekPokeTester(c) {
    private val SoC = c
    
    for (i <- 0 to 10000000) {
        step(1)
    }
    /*
    for (i <- 0 to 1000) {
        step(1)
    }
    
     */
}