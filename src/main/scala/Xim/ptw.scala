package Xim

import chisel3._

/* This is the page table walker for TLB to call */

class ptw extends Module {
    val io = IO(new Bundle{
        // todo: add memory port here
        
        // todo: add tlb entry here
        val TLB_entry = Output(UInt((new TLB_entry).getWidth.W))
        val TLB_entry_valid = Output(UInt(1.W))
    })
    
    // todo:
    io.TLB_entry := 0.U
    io.TLB_entry_valid := 0.U
}
