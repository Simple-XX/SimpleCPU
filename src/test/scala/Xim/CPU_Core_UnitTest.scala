package Xim

import  chisel3._
import chisel3.iotesters.PeekPokeTester
/*
class CPU_Core_Arithmetic_UnitTester(c: CPU_Core) extends PeekPokeTester(c) {
    private val core = c
    step(1) // come into a stable state
    printf("cycle 1\n")
    expect(c.io.inst_req_valid, 1)
    poke(c.io.inst_ack, 1) // made a handshake
    step(1)
    printf("cycle 2\n")
    expect(c.io.inst_addr, 0)
    expect(c.io.inst_ack, 1)
    step(2) // wait a little longer
    printf("cycle 4\n")
    poke(c.io.inst_valid, 1)
    poke(c.io.inst_data, 0x06400093)
    step(2)
    printf("cycle 6\n")
    expect(c.io.reg_wen, 1)
    expect(c.io.reg_waddr, 1)
    expect(c.io.reg_wdata, 100)
}

class CPU_Core_Branch_UnitTester(c: CPU_Core) extends PeekPokeTester(c) {
    private val core = c
    poke(c.io.inst_req_ack, 0)
    poke(c.io.inst_valid, 0)
    poke(c.io.data_req_ack, 0)
    poke(c.io.data_read_valid, 0)
    step(1) // come into a stable state
    print("**********************cycle 1**********************\n")
    expect(c.io.inst_req_valid, 1)
    poke(c.io.inst_ack, 1) // made a handshake
    step(1)
    print("**********************cycle 2**********************\n")
    expect(c.io.inst_addr, 0)
    expect(c.io.inst_ack, 1)
    step(2) // wait a little longer
    print("**********************cycle 4**********************\n")
    poke(c.io.inst_valid, 1)
    poke(c.io.inst_data, 0x00000863)
    step(1)
    print("**********************cycle 5**********************\n")
    poke(c.io.inst_ack, 1)
    poke(c.io.inst_valid, 1)
    poke(c.io.inst_data, 0x06408013) // continue providing inst data
    
    step(1)
    print("**********************cycle 6**********************\n")

    step(1)
    print("**********************cycle 7**********************\n")

    step(1)
    print("**********************cycle 8**********************\n")

    step(1)
    print("**********************cycle 9**********************\n")

    step(1)
    print("**********************cycle 10**********************\n")

    step(1)
    print("**********************cycle 11**********************\n")

}

 */

