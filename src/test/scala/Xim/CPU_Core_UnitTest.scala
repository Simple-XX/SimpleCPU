package Xim

import chisel3.iotesters.PeekPokeTester

class CPU_Core_UnitTester(c: CPU_Core) extends PeekPokeTester(c) {
    private val core = c
    step(1) // come into a stable state
    printf("circle 1\n")
    expect(c.io.inst_req_valid, 1)
    poke(c.io.inst_ack, 1) // made a handshake
    step(1)
    printf("circle 2\n")
    expect(c.io.inst_addr, 0)
    expect(c.io.inst_ack, 1)
    step(2) // wait a little longer
    printf("circle 4\n")
    poke(c.io.inst_valid, 1)
    poke(c.io.inst_data, 0x06400093)
    step(2)
    printf("circle 6\n")
    expect(c.io.reg_wen, 1)
    expect(c.io.reg_waddr, 1)
    expect(c.io.reg_wdata, 100)
}
