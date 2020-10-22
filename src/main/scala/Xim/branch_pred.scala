package Xim

import chisel3._

class branch_pred extends Module {
    val io = IO(new Bundle{
        // whether IF should fetch br target
        val IF_next_branch = Output(UInt(1.W))
        // as the valid condition for EX_taken
        val EX_new_instr_r = Input(UInt(1.W))
        val EX_taken = Input(UInt(1.W))
    })
    val pred_counter = RegInit(0.U(2.W))
    when (io.EX_new_instr_r === 1.U && io.EX_taken === 1.U && pred_counter != 3.U) {
        // if the counter is not currently full and a new taken come in
        pred_counter := pred_counter + 1.U
    } .elsewhen (io.EX_new_instr_r === 1.U && io.EX_taken === 1.U) {
        // if the counter is not empty and a new not taken come in
        pred_counter := pred_counter - 1.U
    }
    io.IF_next_branch := (pred_counter > 1.U)
}

/*
* This module is the helper module for IF to identify the branch instructions
* Note that we do not identify any jmp reg instructions since we cannot fetch the branch target
* */

class branch_target extends Module {
    val io = IO(new Bundle{
        val IF_instr = Input(UInt(32.W))
        val IF_pc = Input(UInt(32.W))
        val is_br = Output(UInt(1.W))
        val br_target = Output(UInt(32.W))
    })
    // note that we handle jal beq bne blt bge bltu bgeu
    val opcode = Wire(UInt(7.W))
    val opcode_d = Wire(UInt(128.W))
    val funct3 = Wire(UInt(3.W))
    val funct3_d = Wire(UInt(8.W))
    val inst_jal = Wire(UInt(1.W))
    val inst_beq = Wire(UInt(1.W))
    val inst_bne = Wire(UInt(1.W))
    val inst_blt = Wire(UInt(1.W))
    val inst_bge = Wire(UInt(1.W))
    val inst_bltu = Wire(UInt(1.W))
    val inst_bgeu = Wire(UInt(1.W))
    val inst_b = Wire(UInt(1.W))
    val inst_j = Wire(UInt(1.W))
    
    opcode := io.IF_instr(6, 0)
    funct3 := io.IF_instr(14, 12)
    val opcode_decoder: decoder_7_128 = Module(new decoder_7_128)
    val funct3_decoder: decoder_3_8 = Module(new decoder_3_8)
    opcode_decoder.io.in := opcode
    opcode_d := opcode_decoder.io.out
    funct3_decoder.io.in := funct3
    funct3_d := funct3_decoder.io.out
    
    inst_jal := (opcode_d(0x6f) === 1.U)
    inst_beq := (opcode_d(0x63) === 1.U) && (funct3_d(0) === 1.U)
    inst_bne := (opcode_d(0x63) === 1.U) && (funct3_d(1) === 1.U)
    inst_blt := (opcode_d(0x63) === 1.U) && (funct3_d(4) === 1.U)
    inst_bge := (opcode_d(0x63) === 1.U) && (funct3_d(5) === 1.U)
    inst_bltu := (opcode_d(0x63) === 1.U) && (funct3_d(6) === 1.U)
    inst_bgeu := (opcode_d(0x63) === 1.U) && (funct3_d(7) === 1.U)
    
    
    inst_b := inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
    inst_j := inst_jal
    
    val jal_target = Wire(UInt(32.W))
    val branch_target = Wire(UInt(32.W))
    
    io.is_br := inst_jal | inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
    when (inst_jal === 1.U) {
        io.br_target := jal_target
    } .otherwise {
        io.br_target := branch_target
    }
    // todo:
    jal_target := 0.U
    branch_target := 0.U
}