package Xim

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class ALUUnitTester(c: ALU) extends PeekPokeTester(c) {
    private val alu = c

    poke(alu.io.alu_src1, 6)
    poke(alu.io.alu_src2, 7)
    poke(alu.io.alu_op, 1) // add
    expect(alu.io.alu_result, 13L)
    expect(alu.io.alu_overflow, 0)

    poke(alu.io.alu_src1, 6)
    poke(alu.io.alu_src2, -2) // 0xfffffffe
    poke(alu.io.alu_op, 1) // add
    expect(alu.io.alu_result, 4L)
    expect(alu.io.alu_overflow, 0)

    poke(alu.io.alu_src1, 6)
    poke(alu.io.alu_src2, 2147483647) // 0xfffffffe
    poke(alu.io.alu_op, 1) // add
    expect(alu.io.alu_result, 2147483653L)
    expect(alu.io.alu_overflow, 1)

    // poke(alu.io.alu_src1, 6) // dont care
    poke(alu.io.alu_src2, 2147483647) // 0xfffffffe
    poke(alu.io.alu_op, 0x800) // add
    expect(alu.io.alu_result, 0x7fff0000L)
    // expect(alu.io.alu_overflow, 0) // dont care

    poke(alu.io.alu_src1, 16)
    poke(alu.io.alu_src2, 0xffff0000L)
    poke(alu.io.alu_op, 0x400) // sra
    expect(alu.io.alu_result, 0xffffffffL)

    poke(alu.io.alu_src1, 16)
    poke(alu.io.alu_src2, 0xffff0000L)
    poke(alu.io.alu_op, 0x200) // sra
    expect(alu.io.alu_result, 0xffffL)
}