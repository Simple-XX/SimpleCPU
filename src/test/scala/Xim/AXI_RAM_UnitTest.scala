package Xim

import chisel3._
import chisel3.iotesters.PeekPokeTester
/*
class AXI_RAM_UnitTester(c: AXI_ram) extends PeekPokeTester(c) {
    private val ram = c
    poke(ram.io.arid, 0)
    poke(ram.io.araddr, 3)
    poke(ram.io.arlen, 0)
    poke(ram.io.arsize, 0) // single byte
    poke(ram.io.arburst, 0)
    poke(ram.io.arlock, 0)
    poke(ram.io.arcache, 0)
    poke(ram.io.arprot, 0)
    poke(ram.io.arvalid, 1)
    step(10)
    
}

 */