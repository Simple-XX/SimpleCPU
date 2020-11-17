package Xim

/* Note that this is still under development */

import Chisel.switch
import Chisel.Cat
import chisel3._
import chisel3.util._

/* This is the page table walker for TLB to call */
/* I would like to use AXI crossbar to split between CPU Core and PTW so we will provide an AXI interface here */
class PTW(width:Int = 64) extends Module {
    val io = IO(new Bundle{
        // SRAM-like inst channel
        val inst_req = Output(UInt(1.W))
        val inst_wr = Output(UInt(1.W))
        val inst_size = Output(UInt(2.W))
        val inst_addr = Output(UInt(width.W))
        val inst_wdata = Output(UInt(width.W))
        val inst_rdata = Input(UInt(width.W))
        val inst_addr_ok = Input(UInt(1.W))
        val inst_data_ok = Input(UInt(1.W))
        
        // TLB related
        val TLB_entry = Output(UInt((new TLB_entry).getWidth.W))
        val TLB_entry_valid = Output(UInt(1.W))
        val req_vaddr = Input(UInt(width.W))
        val req_valid = Input(UInt(1.W))
        val satp_base = Input(UInt(width.W))
    })
    
    val inst_addr = Wire(UInt(width.W))
    val inst_data = Wire(UInt(width.W))
    val inst_req_valid = Wire(UInt(1.W))
    val inst_req_valid_r = RegInit(0.U(1.W))
    val inst_req_ack = Wire(UInt(1.W))
    val inst_valid = Wire(UInt(1.W))
    
    
    // SRAM-like inst channel
    io.inst_req := inst_req_valid
    io.inst_wr := 0.U
    io.inst_size := 3.U // 64 bit
    io.inst_addr := inst_addr
    io.inst_wdata := 0.U
    inst_data := io.inst_rdata
    inst_req_ack := io.inst_addr_ok
    inst_valid := io.inst_data_ok
    
    // begin actual page table walker related
    // the behaviour of a second request before the first request is finished is UNDEFINED
    val vaddr = RegInit(0.U.asTypeOf(new VAddr))
    val req_valid = RegInit(0.U(1.W))
    
    val TLB_entry_r = Reg(new TLB_entry)
    val TLB_entry_valid_set = RegInit(0.U(1.W))
    val TLB_entry_valid = Wire(UInt(1.W))
    val page_table_r = RegInit(0.U.asTypeOf(new PTE))
    val VPN_fetched = Wire(UInt(1.W))
    val is_leaf = Wire(UInt(1.W))
    val addr_handshake = Wire(UInt(1.W))
    val data_handshake = Wire(UInt(1.W))
    
    addr_handshake := (inst_req_valid & inst_req_ack)
    data_handshake := (inst_valid)
    VPN_fetched := data_handshake
    
    when (io.req_valid === 1.U) {
        vaddr := io.req_vaddr.asTypeOf(new VAddr)
        req_valid := io.req_valid
    } .elsewhen (TLB_entry_valid === 1.U) {
        vaddr := 0.U.asTypeOf(new VAddr)
        req_valid := 0.U
    }
    
    when (io.req_valid === 1.U) {
        TLB_entry_valid_set := 0.U
    } .elsewhen (TLB_entry_valid === 1.U) {
        TLB_entry_valid_set := 1.U
    }
    
    // state machine here
    // sInit: the initial state, will move to sVPN2 when new request come, hold the current data
    // sVPN2: fetching the top level page table, when fetched, if leaf go to sInit, otherwise go to sVPN1
    // sVPN1: fetching the second level page table, when fetched, if leaf go to sInit, otherwise go to sVPN0
    // sVPN0: fetching the leaf page table, when fetched, go to sInit
    val sfInit :: sfVPN2 :: sfVPN1 :: sfVPN0 :: Nil = Enum(4)
    val state = RegInit(sfInit)
    val fetched_leaf = Wire(UInt(1.W))
    val fetched_non_leaf = Wire(UInt(1.W))
    fetched_leaf := VPN_fetched === 1.U && is_leaf === 1.U
    fetched_non_leaf := VPN_fetched === 1.U && is_leaf === 0.U
    
    switch (state) {
        is(sfInit) {
            when (io.req_valid === 1.U) {
                state := sfVPN2
            }
        }
        is(sfVPN2) {
            when (fetched_leaf === 1.U) {
                state := sfInit
            } .elsewhen (fetched_non_leaf === 1.U) {
                state := sfVPN1
            }
        }
        is(sfVPN1) {
            when (fetched_leaf === 1.U) {
                state := sfInit
            } .elsewhen (fetched_non_leaf === 1.U) {
                state := sfVPN0
            }
        }
        is(sfVPN0) {
            when (fetched_leaf === 1.U) {
                // software will ensure that this is a leaf page
                state := sfInit
            }
        }
    }
    
    
    inst_req_valid := inst_req_valid_r
    when (io.req_valid === 1.U) {
        inst_req_valid_r := 1.U
    } .elsewhen (fetched_non_leaf === 1.U && state =/= sfVPN0) {
        inst_req_valid_r := 1.U
    } .elsewhen (inst_req_ack === 1.U) {
        inst_req_valid_r := 0.U
    }
    
    
    is_leaf := (page_table_r.X | page_table_r.R | page_table_r.W)
    // inst_addr generation
    // the very beginning address is satp_base + VPN2
    // the following address is PPN + VPNx
    when (state === sfVPN2) {
        inst_addr := io.satp_base + vaddr.VPN2
    } .elsewhen (state === sfVPN1) {
        inst_addr := Cat(page_table_r.PPN2, page_table_r.PPN1, page_table_r.PPN0) + vaddr.VPN1
    } .elsewhen (state === sfVPN0) {
        inst_addr := Cat(page_table_r.PPN2, page_table_r.PPN1, page_table_r.PPN0) + vaddr.VPN0
    } .otherwise {
        inst_addr := 0.U
    }
    
    
    // TLB_entry_r is somewhat complex here
    TLB_entry_r.D := page_table_r.D
    TLB_entry_r.A := page_table_r.A
    TLB_entry_r.G := page_table_r.G
    TLB_entry_r.U := page_table_r.U
    TLB_entry_r.X := page_table_r.X
    TLB_entry_r.W := page_table_r.W
    TLB_entry_r.R := page_table_r.R
    TLB_entry_r.V := page_table_r.V
    
    when (state === sfVPN2 && fetched_leaf === 1.U) {
        TLB_entry_r.page_size := 2.U // 1GB huge page
        TLB_entry_r.VPN2 := vaddr.VPN2
        TLB_entry_r.VPN1 := 0.U
        TLB_entry_r.VPN0 := 0.U
        TLB_entry_r.PPN2 := page_table_r.PPN2
        // TODO: add exceptions here
        // note that a exception should be thrown here if PPN1 or PPN0 is non-zero
        TLB_entry_r.PPN1 := 0.U
        TLB_entry_r.PPN0 := 0.U

    } .elsewhen (state === sfVPN1 && fetched_leaf === 1.U) {
        TLB_entry_r.page_size := 2.U // 1GB huge page
        TLB_entry_r.VPN2 := vaddr.VPN2
        TLB_entry_r.VPN1 := vaddr.VPN1
        TLB_entry_r.VPN0 := 0.U
        TLB_entry_r.PPN2 := page_table_r.PPN2
        // TODO: add exceptions here
        // note that a exception should be thrown here if PPN0 is non-zero
        TLB_entry_r.PPN1 := page_table_r.PPN1
        TLB_entry_r.PPN0 := 0.U
    } .elsewhen (state === sfVPN0 && fetched_leaf === 1.U) {
        TLB_entry_r.page_size := 2.U // 1GB huge page
        TLB_entry_r.VPN2 := vaddr.VPN2
        TLB_entry_r.VPN1 := vaddr.VPN1
        TLB_entry_r.VPN0 := vaddr.VPN0
        TLB_entry_r.PPN2 := page_table_r.PPN2
        // TODO: add exceptions here
        // note that a exception should be thrown here if the PPN1 or PPN0 is non-zero
        TLB_entry_r.PPN1 := page_table_r.PPN1
        TLB_entry_r.PPN0 := page_table_r.PPN0
    }
    
    io.TLB_entry := TLB_entry_r.asUInt()
    io.TLB_entry_valid := TLB_entry_valid & !TLB_entry_valid_set
    TLB_entry_valid := fetched_leaf
    
    when (data_handshake === 1.U) {
        //page_table_r := inst_data
    }
    
    
    // todo
    VPN_fetched := 0.U
}

object PTW extends App {
    chisel3.Driver.execute(args, () => new PTW(64))
}