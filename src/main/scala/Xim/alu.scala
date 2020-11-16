// See README.md for license details.

package Xim

import chisel3._
import chisel3.internal.firrtl.Width

class MySlt(val rv_width: Int = 64) extends Bundle {
    val zero = UInt((rv_width - 1).W)
    val sig = UInt(1.W)
}

class MyLui extends Bundle {
    val upper = UInt(52.W)
    val zero = UInt((12).W)
}


/**
 * ALU module
 */
class ALU(val rv_width: Int = 64) extends Module {
    val io = IO(new Bundle {
        val alu_src1        = Input(UInt(rv_width.W))
        val alu_src2        = Input(UInt(rv_width.W))
        val alu_op          = Input(UInt(14.W))
        val alu_result      = Output(UInt(rv_width.W))
        val alu_overflow    = Output(UInt(1.W))
    })
    
    
    val op_add = Wire(UInt(1.W))
    val op_sub = Wire(UInt(1.W))
    val op_slt = Wire(UInt(1.W))
    val op_sltu = Wire(UInt(1.W))
    val op_and = Wire(UInt(1.W))
    val op_nor = Wire(UInt(1.W))
    val op_or = Wire(UInt(1.W))
    val op_xor = Wire(UInt(1.W))
    val op_sll = Wire(UInt(1.W))
    val op_srl = Wire(UInt(1.W))
    val op_sra = Wire(UInt(1.W))
    val op_lui = Wire(UInt(1.W))
    val op_srl_w = Wire(UInt(1.W))
    val op_sra_w = Wire(UInt(1.W))
    
    op_add := io.alu_op(0)
    op_sub := io.alu_op(1)
    op_slt := io.alu_op(2)
    op_sltu := io.alu_op(3)
    op_and := io.alu_op(4)
    op_nor := io.alu_op(5)
    op_or := io.alu_op(6)
    op_xor := io.alu_op(7)
    op_sll := io.alu_op(8)
    op_srl := io.alu_op(9)
    op_sra := io.alu_op(10)
    op_lui := io.alu_op(11)
    op_srl_w := io.alu_op(12)
    op_sra_w := io.alu_op(13)
    
    val add_sub_result = Wire(UInt(rv_width.W))
    val slt_result = Wire(new MySlt(rv_width))
    val sltu_result = Wire(new MySlt(rv_width))
    val and_result = Wire(UInt(rv_width.W))
    val nor_result = Wire(UInt(rv_width.W))
    val or_result = Wire(UInt(rv_width.W))
    val xor_result = Wire(UInt(rv_width.W))
    val lui_result = Wire(new MyLui) // will extend
    val lui_result_s = Wire(SInt(rv_width.W))
    // Note that 6.W is not portable for non-64 bit
    val shift_off = Wire(UInt(6.W))
    val sll_result = Wire(UInt(rv_width.W))
    val srl_result = Wire(UInt(rv_width.W))
    val sra_result = Wire(SInt(rv_width.W))
    
    val srl_w_result = Wire(UInt(32.W))
    val sra_w_result = Wire(SInt(32.W))
    
    val adder_a = Wire(UInt(rv_width.W))
    val adder_b = Wire(UInt(rv_width.W))
    val adder_cin = Wire(UInt(1.W))
    val adder_result = Wire(UInt(rv_width.W))
    val adder_cout = Wire(UInt(rv_width.W))
    val adder_ori_result = Wire(UInt((rv_width + 1).W))
    
    adder_a := io.alu_src1
    
    when (op_sub === 1.U || op_sltu === 1.U) {
        adder_b := ~io.alu_src2
    } .otherwise {
        adder_b := io.alu_src2
    }
    
    when (op_sub === 1.U || op_sltu === 1.U) {
        adder_cin := 1.U
    } .otherwise {
        adder_cin := 0.U
    }
    
    adder_ori_result := adder_a +& adder_b +& adder_cin;
    
    adder_cout := adder_ori_result(rv_width)
    adder_result := adder_ori_result
    
    when (op_add === 1.U) {
        io.alu_overflow := (io.alu_src1((rv_width - 1).U) & io.alu_src2((rv_width - 1).U) & ~adder_result((rv_width - 1).U)) |
          (~io.alu_src1((rv_width - 1).U) & ~io.alu_src2((rv_width - 1).U) & adder_result((rv_width - 1).U))
    }.elsewhen (op_sub === 1.U) {
        io.alu_overflow := (io.alu_src1((rv_width - 1).U) & ~io.alu_src2((rv_width - 1).U) & ~adder_result((rv_width - 1).U)) |
          (~io.alu_src1((rv_width - 1).U) & io.alu_src2((rv_width - 1).U) & adder_result((rv_width - 1).U))
    } .otherwise {
        io.alu_overflow := 0.U;
    }
    
    add_sub_result := adder_result
    
    slt_result.zero := 0.U
    when (io.alu_src1.asSInt() < io.alu_src2.asSInt()) {
        slt_result.sig := 1.U
    } .otherwise {
        slt_result.sig := 0.U
    }
    
    
    sltu_result.zero := 0.U
    sltu_result.sig := ~adder_cout
    
    and_result := io.alu_src1 & io.alu_src2
    or_result := io.alu_src1 | io.alu_src2
    nor_result := ~or_result
    xor_result := io.alu_src1 ^ io.alu_src2
    
    // need testing here
    lui_result.upper := io.alu_src2((rv_width - 1), 12)
    lui_result.zero := 0.U
    lui_result_s := lui_result.asUInt().asSInt()
    
    shift_off := io.alu_src2
    sll_result := io.alu_src1 << shift_off
    srl_result := io.alu_src1 >> shift_off
    sra_result := io.alu_src1.asSInt >> shift_off
    
    srl_w_result := io.alu_src1(31, 0) >> shift_off
    sra_w_result := io.alu_src1(31, 0).asSInt >> shift_off
    
    when (op_add === 1.U || op_sub === 1.U) {
        io.alu_result := add_sub_result
    } .elsewhen (op_slt === 1.U) {
        io.alu_result := slt_result.asUInt
    } .elsewhen (op_sltu === 1.U) {
        io.alu_result := sltu_result.asUInt
    } .elsewhen (op_and === 1.U) {
        io.alu_result := and_result
    } .elsewhen (op_nor === 1.U) {
        io.alu_result := nor_result
    } .elsewhen (op_or === 1.U) {
        io.alu_result := or_result
    } .elsewhen (op_xor === 1.U) {
        io.alu_result := xor_result
    } .elsewhen (op_lui === 1.U) {
        io.alu_result := lui_result_s.asUInt
    } .elsewhen (op_sll === 1.U) {
        io.alu_result := sll_result
    } .elsewhen (op_srl === 1.U) {
        io.alu_result := srl_result
    } .elsewhen (op_sra === 1.U) {
        io.alu_result := sra_result.asUInt
    } .elsewhen (op_sra_w === 1.U) {
        io.alu_result := sra_w_result.asUInt
    } .elsewhen (op_srl_w === 1.U) {
        io.alu_result := srl_w_result.asUInt
    } .otherwise {
        io.alu_result := 0.U;
    }
}


object ALU extends App {
    chisel3.Driver.execute(args, () => new ALU(64))
}