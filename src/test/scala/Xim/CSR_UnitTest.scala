package Xim

import chisel3._
import chisel3.iotesters.PeekPokeTester

class CSR_UnitTester(c: CSR) extends PeekPokeTester(c) {
    private val csr = c
    // EPC
    poke(csr.io.es_csr_wr, 1)
    poke(csr.io.es_csr_write_num, 0x341)
    poke(csr.io.es_csr_write_data, 0x2333)
    poke(csr.io.es_csr_read_num, 0x341)
    step(1)
    expect(csr.io.es_csr_read_data, 0x2333)
    
    
    
}
