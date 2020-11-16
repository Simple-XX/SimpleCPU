package Xim

import scala.util.control.Breaks._
import scala.io.Source
import chisel3.iotesters.PeekPokeTester

class SoC_UnitTester_Type1(c: SoC) extends PeekPokeTester(c) {
    // private val SoC = c
    var expected_pc: BigInt = 0
    var prev_pc: BigInt = 0
    var curr_pc: BigInt = 0
    var curr_inst: BigInt = 0
    var inst_count: BigInt = 0
    val filename = "/Users/cgk/ownCloud/课程/一生一芯/ict/golden_trace.txt"
    step(16) // tricky here: reset to normal
    breakable {
        for (line <- Source.fromFile(filename).getLines) {
            inst_count += 1
            val tmp_line = line.substring(0, 8)
            expected_pc = java.lang.Long.parseLong(tmp_line, 16)
            // we have fetched our expected pc from trace file, trying to figure out our actual pc
            do {
                step(1)
                curr_pc = peek(c.io.es_pc)
                curr_inst = peek(c.io.es_instr)
                // if we are still in the previous instruction or in invalid slot, continue
            } while (curr_pc == prev_pc || curr_inst == 0x33)
            
            if (curr_pc != expected_pc) {
                printf(s"Fail at inst_count %x, expected pc = %x, actual pc = %x\n", inst_count, expected_pc, curr_pc)
                break()
            }
            //printf(s"current pc = %x, ", curr_pc)
            prev_pc = curr_pc
            //printf(s"debug: ${inst_count}\n")
        }
    }
}

class SoC_UnitTester_Type2(c: SoC) extends PeekPokeTester(c) {
    // testbench that ends with an ecall

    var i:Int = 0
    
    while (i < 1000000 && peek(c.io.es_instr) != BigInt(0x73)) {
        step(1)
        i += 1
    }
    expect(c.io.es_reg_a0, 0) // expect a0 to be zero
    
    /* after test, trigger an exception as a test end */
    /* still run a few steps */
    step(200)
}

class SoC_UnitTester_Type3(c: SoC) extends PeekPokeTester(c) {
    // mostly type 1, without trace compare enabled
    var expected_pc: BigInt = 0
    var prev_pc: BigInt = 0
    var curr_pc: BigInt = 0
    var curr_inst: BigInt = 0
    var inst_count: BigInt = 0
    val filename = "/Users/cgk/ownCloud/课程/一生一芯/ict/golden_trace.txt"
    step(16) // tricky here: reset to normal
    var i = 0
    breakable {
        for (i <- 1 to 500000) {
            inst_count += 1
            // we have fetched our expected pc from trace file, trying to figure out our actual pc
            do {
                step(1)
                curr_pc = peek(c.io.es_pc)
                curr_inst = peek(c.io.es_instr)
                // if we are still in the previous instruction or in invalid slot, continue
            } while (curr_pc == prev_pc || curr_inst == 0x33)
            
            //printf(s"current pc = %x, ", curr_pc)
            prev_pc = curr_pc
            //printf(s"debug: ${inst_count}\n")
        }
    }
}