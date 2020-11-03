package Xim

import Chisel.{OHToUInt, log2Ceil}
import chisel3._
import chisel3.util

/* a variable entry count TLB module */
class tlb(TLB_entry_count: Int, width: Int) extends Module {
    val io = IO(new Bundle {
        val VAddr = Input(UInt(64.W))
        val PAddr = Output(UInt(64.W))
        val TLB_ex = Output(UInt(1.W))
        val TLB_excode = Output(UInt(5.W))
        // TODO: add memory port here
    })
    // we will drive PTW in this module
    val tlb_entry = RegInit(VecInit(Seq.fill(TLB_entry_count)(0.U.asTypeOf(new TLB_entry))))
    val tlb_match = Wire(Vec(TLB_entry_count, UInt(1.W)))
    val tlb_match_index = WireInit(OHToUInt(tlb_match.asUInt()))
    val tlb_matched = Wire(UInt(1.W))
    
    val tlb_VAddr = WireInit(io.VAddr.asTypeOf(new VAddr))
    val tlb_PAddr = Wire(new PAddr)
    
    io.PAddr := tlb_PAddr.asUInt()
    
    val tlb_write_index = RegInit(0.U(log2Ceil(TLB_entry_count).W))
    
    // page table walker
    val PTW = Module(new ptw(width))
    val tlb_write = Wire(UInt(1.W))
    val tlb_entry_ptw = Wire(UInt((new TLB_entry).getWidth.W))
    tlb_write := PTW.io.TLB_entry_valid
    tlb_entry_ptw := PTW.io.TLB_entry
    
    for (i <- 0 until TLB_entry_count) {
        when (tlb_entry(i).V === 1.U && (
            (tlb_entry(i).page_size === 0.U &&
              tlb_VAddr.VPN0 === tlb_entry(i).VPN0 &&
              tlb_VAddr.VPN1 === tlb_entry(i).VPN1 &&
              tlb_VAddr.VPN2 === tlb_entry(i).VPN2) ||
            (tlb_entry(i).page_size === 1.U &&
              tlb_VAddr.VPN1 === tlb_entry(i).VPN1 &&
              tlb_VAddr.VPN2 === tlb_entry(i).VPN2) ||
            (tlb_entry(i).page_size === 2.U &&
                tlb_VAddr.VPN2 === tlb_entry(i).VPN2)
          )) {
            // type1: 4KB
            tlb_match(i) := 1.U
        } .otherwise {
            tlb_match(i) := 0.U
        }
    }
    
    tlb_matched := (tlb_match.asUInt() =/= 0.U)
    
    // todo:
    tlb_PAddr.offset := 0.U
    tlb_PAddr.PPN0 := 0.U
    tlb_PAddr.PPN1 := 0.U
    tlb_PAddr.PPN2 := 0.U
    tlb_PAddr.Zero := 0.U
    io.TLB_ex := 0.U
    io.TLB_excode := 0.U
}

object tlb extends App {
    chisel3.Driver.execute(args, () => new tlb(32, 64))
}