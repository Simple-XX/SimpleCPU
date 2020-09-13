package Xim

import chisel3._

class I_imm_bundle extends Bundle {
  val inst_31 = UInt(1.W)
  val inst_30_25 = UInt(6.W)
  val inst_24_21 = UInt(4.W)
  val inst_20 = UInt(1.W)
}

class S_imm_bundle extends Bundle {
  val inst_31 = UInt(1.W)
  val inst_30_25 = UInt(6.W)
  val inst_11_8 = UInt(4.W)
  val inst_7 = UInt(1.W)
}

class B_imm_bundle extends Bundle {
  val inst_31 = UInt(1.W)
  val inst_7 = UInt(1.W)
  val inst_30_25 = UInt(6.W)
  val inst_11_8 = UInt(4.W)
  val zero = UInt(1.W)
}

class U_imm_bundle extends Bundle {
  val inst_31 = UInt(1.W)
  val inst_30_20 = UInt(11.W)
  val inst_19_12 = UInt(8.W)
  val zero = UInt(12.W)
}

class J_imm_bundle extends Bundle {
  val inst_31 = UInt(1.W)
  val inst_19_12 = UInt(8.W)
  val inst_20 = UInt(1.W)
  val inst_30_25 = UInt(6.W)
  val inst_24_21 = UInt(4.W)
  val zero = UInt(1.W)
}

class regfile_raddr extends Bundle {
  val raddr1 = UInt(5.W)
  val raddr2 = UInt(5.W)
}

class decoder_7_128 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(7.W))
    val out = Output(UInt(128.W))
  })
  val tmp = Wire(Vec(128, UInt(1.W)))
  for (i <- 0 until 128) {
    when (io.in === i.U) {
      tmp(i) := 1.U
    } .otherwise {
      tmp(i) := 0.U
    }
  }
  io.out := tmp.asUInt
}

class decoder_3_8 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(3.W))
    val out = Output(UInt(8.W))
  })
  val tmp = Wire(Vec(8, UInt(1.W)))
  for (i <- 0 until 8) {
    when (io.in === i.U) {
      tmp(i) := 1.U
    } .otherwise {
      tmp(i) := 0.U
    }
  }
  io.out := tmp.asUInt
}

class CPU_IF extends Module {
  val io = IO(new Bundle {
    val inst_addr        = Output(UInt(32.W))
    val inst_req_valid   = Output(UInt(1.W))
    val inst_req_ack     = Input(UInt(1.W))

    val inst_data        = Input(UInt(32.W))
    val inst_valid       = Input(UInt(1.W))
    val inst_ack         = Output(UInt(1.W))

    val es_allowin       = Input(UInt(1.W))
    val inst_reload      = Input(UInt(1.W))
    val fs_pc            = Output(UInt(32.W))
    val fs_inst          = Output(UInt(32.W))
    val fs_ex            = Output(UInt(1.W))
    val fs_excode        = Output(UInt(5.W)) // maybe should be longer?

    val br_valid         = Input(UInt(1.W))
    val br_target        = Input(UInt(32.W))
  })

  val fs_valid = Reg(UInt(1.W))
  val fs_allowin = Wire(UInt(1.W))
  val fs_ready_go = Wire(UInt(1.W))

  val next_pc = Wire(UInt(32.W))
  val fs_pc_r = RegInit((0xfffffffcL).U(32.W))

  io.fs_pc := fs_pc_r

  when (io.br_valid === 1.U) {
    next_pc := io.br_target
  } .otherwise {
    next_pc := fs_pc_r + 4.U;
  }
  
  io.inst_addr := next_pc
  
  when (io.inst_req_valid === 1.U && io.inst_req_ack === 1.U) {
    fs_pc_r := next_pc
  }

  io.inst_req_valid := (io.es_allowin === 1.U) && !(io.fs_ex === 1.U) // maybe we should consider some reload signals in the future


  
  
}

class CPU_EX extends Module {
  val io = IO(new Bundle {
    val data_addr        = Output(UInt(32.W))
    val data_write       = Output(UInt(1.W))
    val data_read         = Output(UInt(1.W))

    val data_write_data  = Output(UInt(32.W))
    val data_wstrb       = Output(UInt(4.W))

    val data_req_ack      = Input(UInt(1.W))

    val data_read_data   = Input(UInt(32.W))
    val data_read_valid  = Input(UInt(1.W))
    val data_data_ack    = Output(UInt(1.W))

    val es_allowin       = Output(UInt(1.W))
    val inst_reload      = Output(UInt(1.W))

    val fs_pc            = Output(UInt(32.W))
    val fs_inst          = Output(UInt(32.W))

    val br_valid         = Output(UInt(1.W))
    val br_target        = Output(UInt(32.W))
  })

  val es_valid = RegInit(0.U(1.W))
  // es_allowin as Output
  val es_ready_go = Wire(UInt(1.W))
  val es_pc = Reg(UInt(32.W))
  val es_instr = Reg(UInt(32.W))



  // decode related 
  val es_load = Wire(UInt(1.W))
  val es_store = Wire(UInt(1.W))
  val es_branch = Wire(UInt(1.W))
  
  val inst_lui = Wire(UInt(1.W))
  val inst_auipc = Wire(UInt(1.W))
  val inst_jal = Wire(UInt(1.W))
  val inst_jalr = Wire(UInt(1.W))
  val inst_beq = Wire(UInt(1.W))
  val inst_bne = Wire(UInt(1.W))
  val inst_blt = Wire(UInt(1.W))
  val inst_bge = Wire(UInt(1.W))
  val inst_bltu = Wire(UInt(1.W))
  val inst_bgeu = Wire(UInt(1.W))
  val inst_lb = Wire(UInt(1.W))
  val inst_lh = Wire(UInt(1.W))
  val inst_lw = Wire(UInt(1.W))
  val inst_lbu = Wire(UInt(1.W))
  val inst_lhu = Wire(UInt(1.W))
  val inst_sb = Wire(UInt(1.W))
  val inst_sh = Wire(UInt(1.W))
  val inst_sw = Wire(UInt(1.W))
  val inst_addi = Wire(UInt(1.W))
  val inst_slti = Wire(UInt(1.W))
  val inst_sltiu = Wire(UInt(1.W))
  val inst_xori = Wire(UInt(1.W))
  val inst_ori = Wire(UInt(1.W))
  val inst_andi = Wire(UInt(1.W))
  val inst_slli = Wire(UInt(1.W))
  val inst_srli = Wire(UInt(1.W))
  val inst_srai = Wire(UInt(1.W))
  val inst_add = Wire(UInt(1.W))
  val inst_sub = Wire(UInt(1.W))
  val inst_sll = Wire(UInt(1.W))
  val inst_slt = Wire(UInt(1.W))
  val inst_sltu = Wire(UInt(1.W))
  val inst_xor = Wire(UInt(1.W))
  val inst_srl = Wire(UInt(1.W))
  val inst_sra = Wire(UInt(1.W))
  val inst_or = Wire(UInt(1.W))
  val inst_and = Wire(UInt(1.W))
  val inst_fence = Wire(UInt(1.W))
  val inst_ecall = Wire(UInt(1.W))
  val inst_ebreak = Wire(UInt(1.W))
  val inst_csrrw = Wire(UInt(1.W))
  val inst_csrrs = Wire(UInt(1.W))
  val inst_csrrc = Wire(UInt(1.W))
  val inst_csrrwi = Wire(UInt(1.W))
  val inst_csrrsi = Wire(UInt(1.W))
  val inst_csrrci = Wire(UInt(1.W))
  val inst_fence_i = Wire(UInt(1.W))
  val inst_reserved = Wire(UInt(1.W)) // reserved instruction

  when (es_load === 1.U || es_store === 1.U) {
    es_ready_go := io.data_data_ack
  } .otherwise {
    es_ready_go := 1.U
  }

  io.es_allowin := (!(es_valid === 1.U)) || (es_ready_go === 1.U)

  // inst coding zone
  val opcode = Wire(UInt(7.W))
  val opcode_d = Wire(UInt(128.W))
  val funct7 = Wire(UInt(7.W))
  val funct7_d = Wire(UInt(128.W))
  val funct3 = Wire(UInt(3.W))
  val funct3_d = Wire(UInt(8.W))
  val rs1 = Wire(UInt(5.W))
  val rs2 = Wire(UInt(5.W))
  val rd = Wire(UInt(5.W))
  
  val I_imm = Wire(SInt(32.W)) // sign extend
  val S_imm = Wire(SInt(32.W)) // sign extend
  val B_imm = Wire(SInt(32.W)) // sign extend
  val U_imm = Wire(UInt(32.W)) // imm to upper
  val J_imm = Wire(SInt(32.W)) // sign extend
  val I_imm_b = Wire(new I_imm_bundle)
  val S_imm_b = Wire(new S_imm_bundle)
  val B_imm_b = Wire(new B_imm_bundle)
  val U_imm_b = Wire(new U_imm_bundle)
  val J_imm_b = Wire(new J_imm_bundle)
  val inst_imm = Wire(UInt(32.W))
  val inst_i := Wire(UInt(1.W))
  val inst_s := Wire(UInt(1.W))
  val inst_b := Wire(UInt(1.W))
  val inst_u := Wire(UInt(1.W))
  val inst_j := Wire(UInt(1.W))
  val inst_r := Wire(UInt(1.W))

  inst_i := inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_addi | inst_slti | inst_sltiu | inst_xori | inst_ori | inst_andi | inst_fence_i
  inst_s := inst_sb | inst_sh | inst_sw
  inst_b := inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
  inst_u := inst_lui | inst_auipc | 
  inst_j := inst_jal |
  inst_r := inst_slli | inst_srli | inst_srai | inst_add | inst_sub | inst_sll | inst_slt | inst_sltu | inst_xor | inst_srl | inst_sra | inst_or | inst_and

  when (inst_i === 1.U) {
    inst_imm := I_imm.asUInt
  } .elsewhen (inst_s === 1.U) {
    inst_imm := S_imm.asUInt
  } .elsewhen (inst_b === 1.U) {
    inst_imm := B_imm.asUInt
  } .elsewhen (inst_u === 1.U) {
    inst_imm := U_imm
  } .elsewhen (inst_j === 1.U) {
    inst_imm := J_imm.asUInt
  } .otherwise {
    inst_imm := 0.U
  }

  opcode := es_instr(6, 0)
  funct7 := es_instr(31, 25)
  funct3 := es_instr(14, 12)
  rs1 := es_instr(19, 15)
  rs2 := es_instr(24, 20)
  rd := es_instr(11, 7)
  I_imm := I_imm_b.asSInt
  S_imm := S_imm_b.asSInt
  B_imm := B_imm_b.asSInt
  U_imm := U_imm_b.asUInt
  J_imm := J_imm_b.asSInt

  I_imm_b.inst_31 := es_instr(31)
  I_imm_b.inst_30_25 := es_instr(30, 25)
  I_imm_b.inst_24_21 := es_instr(24, 21)
  I_imm_b.inst_20 := es_instr(20)

  S_imm_b.inst_31 := es_instr(31)
  S_imm_b.inst_30_25 := es_instr(30, 25)
  S_imm_b.inst_11_8 := es_instr(11, 8)
  S_imm_b.inst_7 := es_instr(7)

  B_imm_b.inst_31 := es_instr(31)
  B_imm_b.inst_7 := es_instr(7)
  B_imm_b.inst_30_25 := es_instr(30, 25)
  B_imm_b.inst_11_8 := es_instr(11, 8)
  B_imm_b.zero := 0.U

  U_imm_b.inst31 := es_instr(31)
  U_imm_b.inst_30_20 := es_instr(30, 20)
  U_imm_b.instr_19_12 := es_instr(19, 12)
  U_imm_b.zero := 0.U

  J_imm_b.inst31 := es_instr(31)
  J_imm_b.inst_19_12 := es_instr(19, 12)
  J_imm_b.inst_20 := es_instr(20)
  J_imm_b.inst_30_25 := es_instr(30, 25)
  J_imm_b.inst_24_21 := es_instr(24, 21)
  J_imm_b.zero := 0.U



  val opcode_decoder = Module(new decoder_7_128)
  val funct7_decoder = Module(new decoder_7_128)
  val funct3_decoder = Module(new decoder_3_8)

  opcode_decoder.io.in := opcode
  opcode_d := opcode_decoder.io.out

  funct7_decoder.io.in := funct7
  funct7_d := funct7_decoder.io.out

  funct3_decoder.io.in := funct3
  funct3_d := funct3_decoder.io.out

  inst_lui := (opcode_d(0x37) === 1.U)
  inst_auipc := (opcode_d(0x17) === 1.U)
  inst_jal := (opcode_d(0x6f) === 1.U)
  inst_jalr := (opcode_d(0x67) === 1.U) && (funct3_d(0) === 1.U)
  inst_beq := (opcode_d(0x63) === 1.U) && (funct3_d(0) === 1.U)
  inst_bne := (opcode_d(0x63) === 1.U) && (funct3_d(1) === 1.U)
  inst_blt := (opcode_d(0x63) === 1.U) && (funct3_d(4) === 1.U)
  inst_bge := (opcode_d(0x63) === 1.U) && (funct3_d(5) === 1.U)
  inst_bltu := (opcode_d(0x63) === 1.U) && (funct3_d(6) === 1.U)
  inst_bgeu := (opcode_d(0x63) === 1.U) && (funct3_d(7) === 1.U)
  inst_lb := (opcode_d(0x3) === 1.U) && (funct3_d(0) === 1.U)
  inst_lh := (opcode_d(0x3) === 1.U) && (funct3_d(1) === 1.U)
  inst_lw := (opcode_d(0x3) === 1.U) && (funct3_d(2) === 1.U)
  inst_lbu := (opcode_d(0x3) === 1.U) && (funct3_d(4) === 1.U)
  inst_lhu := (opcode_d(0x3) === 1.U) && (funct3_d(5) === 1.U)
  inst_sb := (opcode_d(0x23) === 1.U) && (funct3_d(0) === 1.U)
  inst_sh := (opcode_d(0x23) === 1.U) && (funct3_d(1) === 1.U)
  inst_sw := (opcode_d(0x23) === 1.U) && (funct3_d(2) === 1.U)
  inst_addi := (opcode_d(0x13) === 1.U) && (funct3_d(0) === 1.U)
  inst_slti := (opcode_d(0x13) === 1.U) && (funct3_d(2) === 1.U)
  inst_sltiu := (opcode_d(0x13) === 1.U) && (funct3_d(3) === 1.U)
  inst_xori := (opcode_d(0x13) === 1.U) && (funct3_d(4) === 1.U)
  inst_ori := (opcode_d(0x13) === 1.U) && (funct3_d(6) === 1.U)
  inst_andi := (opcode_d(0x13) === 1.U) && (funct3_d(7) === 1.U)
  inst_slli := (opcode_d(0x13) === 1.U) && (funct3_d(1) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_srli := (opcode_d(0x13) === 1.U) && (funct3_d(5) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_srai := (opcode_d(0x13) === 1.U) && (funct3_d(5) === 1.U) && (funct7_d(0x20) === 1.U)
  inst_add := (opcode_d(0x33) === 1.U) && (funct3_d(0) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_sub := (opcode_d(0x33) === 1.U) && (funct3_d(0) === 1.U) && (funct7_d(0x20) === 1.U)
  inst_sll := (opcode_d(0x33) === 1.U) && (funct3_d(1) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_slt := (opcode_d(0x33) === 1.U) && (funct3_d(2) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_sltu := (opcode_d(0x33) === 1.U) && (funct3_d(3) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_xor := (opcode_d(0x33) === 1.U) && (funct3_d(4) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_srl := (opcode_d(0x33) === 1.U) && (funct3_d(5) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_sra := (opcode_d(0x33) === 1.U) && (funct3_d(5) === 1.U) && (funct7_d(0x20) === 1.U)
  inst_or := (opcode_d(0x33) === 1.U) && (funct3_d(6) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_and := (opcode_d(0x33) === 1.U) && (funct3_d(7) === 1.U) && (funct7_d(0x0) === 1.U)
  inst_fence := (opcode_d(0xf) === 1.U) && (funct3_d(0) === 1.U)
  inst_ecall := (opcode_d(0x73) === 1.U) && (funct3_d(0) === 1.U) && (funct7_d(0x0) === 1.U) && (rs1 === 0.U) && (rs2 === 0.U) && (rd === 0.U)
  inst_ebreak := (opcode_d(0x73) === 1.U) && (funct3_d(0) === 1.U) && (funct7_d(0x0) === 1.U) && (rs1 === 0.U) && (rs2 === 1.U) && (rd === 0.U)
  inst_fence_i := (opcode_d(0xf) === 1.U) && (funct3_d(1) === 1.U)
  inst_csrrw := (opcode_d(0x73) === 1.U) && (funct3_d(1) === 1.U)
  inst_csrrs := (opcode_d(0x73) === 1.U) && (funct3_d(2) === 1.U)
  inst_csrrc := (opcode_d(0x73) === 1.U) && (funct3_d(3) === 1.U)
  inst_csrrwi := (opcode_d(0x73) === 1.U) && (funct3_d(5) === 1.U)
  inst_csrrsi := (opcode_d(0x73) === 1.U) && (funct3_d(6) === 1.U)
  inst_csrrci := (opcode_d(0x73) === 1.U) && (funct3_d(7) === 1.U)

  inst_reserved := !(
    // RV32I
    inst_lui === 1.U || inst_auipc === 1.U || inst_jal === 1.U ||
    inst_jalr === 1.U || inst_beq === 1.U || inst_bne === 1.U ||
    inst_blt === 1.U || inst_bge === 1.U || inst_bltu === 1.U ||
    inst_bgeu === 1.U || inst_lb === 1.U || inst_lh === 1.U ||
    inst_lw === 1.U || inst_lbu === 1.U || inst_lhu === 1.U || inst_sb === 1.U || inst_sh === 1.U || inst_sw === 1.U ||
    inst_addi === 1.U || inst_slti === 1.U || inst_sltiu === 1.U ||
    inst_xori === 1.U || inst_ori === 1.U || inst_andi === 1.U ||
    inst_slli === 1.U || inst_srli === 1.U || inst_srai === 1.U ||
    inst_add === 1.U || inst_sub === 1.U || inst_sll === 1.U ||
    inst_slt === 1.U || inst_sltu === 1.U || inst_xor === 1.U ||
    inst_srl === 1.U || inst_sra === 1.U || inst_or === 1.U ||
    inst_and === 1.U || inst_fence === 1.U || inst_ecall === 1.U ||
    inst_ebreak === 1.U ||
    // RV32Zifencei
    inst_fence_i === 1.U ||

    // RV32Zicsr
    inst_csrrw === 1.U || inst_csrrs === 1.U || inst_csrrc === 1.U || inst_csrrc === 1.U || inst_csrrwi === 1.U || inst_csrrsi === 1.U || inst_csrrci === 1.U
  )

  // Execute stage
  val es_alu = Module(new ALU)
  val alu_result = Wire(UInt(32.W))
  val alu_overflow = Wire(UInt(1.W))
  val alu_op = Wire(UInt(12.W))
  val es_src1_rs1 = Wire(UInt(1.W))
  val es_src1_imm = Wire(UInt(1.W))
  val es_src2_rs2 = Wire(UInt(1.W))
  val es_src2_pc = Wire(UInt(1.W))
  val es_src2_imm = Wire(UInt(1.W))
  val es_alu_op_add = Wire(UInt(1.W))
  val es_alu_op_sub = Wire(UInt(1.W))
  val es_alu_op_slt = Wire(UInt(1.W))
  val es_alu_op_sltu = Wire(UInt(1.W))
  val es_alu_op_and = Wire(UInt(1.W))
  val es_alu_op_nor = Wire(UInt(1.W))
  val es_alu_op_or = Wire(UInt(1.W))
  val es_alu_op_xor = Wire(UInt(1.W))
  val es_alu_op_sll = Wire(UInt(1.W))
  val es_alu_op_srl = Wire(UInt(1.W))
  val es_alu_op_sra = Wire(UInt(1.W))
  val es_alu_op_lui = Wire(UInt(1.W))

  es_src1_imm := inst_auipc | inst_jal | inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
  es_src1_rs1 := inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_sb | inst_sh | inst_sw | inst_addi | inst_add | inst_sub | inst_slti | inst_slt | inst_sltiu | inst_sltu | inst_andi | inst_and | inst_ori | inst_or | inst_xori | inst_xor | inst_slli | inst_sll | inst_srli | inst_srl | inst_srai | inst_sra | inst_lui // LUI will not use src1
  es_src2_rs2 := inst_add | inst_sub | inst_slt | inst_sltu | inst_and | inst_or | inst_xor | inst_sll | inst_srl | inst_sra
  es_src2_rs2_self := inst_slli | inst_srli | inst_srai
  es_src2_pc := inst_auipc | inst_jal | inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
  es_src2_imm := inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_sb | inst_sh | inst_sw | inst_addi | inst_slti | inst_sltiu | inst_andi | inst_ori | inst_xori | inst_lui

  es_alu_op_add := (inst_auipc | inst_jal | inst_jalr | inst_beq | inst_bne | inst_blt | inst_bge |
                    inst_bltu | inst_bgeu | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu |
                    inst_sb | inst_sh | inst_sw | inst_addi | inst_add)
  es_alu_op_sub := inst_sub
  es_alu_op_slt := inst_slti | inst_slt
  es_alu_op_sltu := inst_sltiu | inst_sltu
  es_alu_op_and := inst_andi | inst_and
  es_alu_op_nor := 0.U // unused for risc-v
  es_alu_op_or := inst_ori | inst_or
  es_alu_op_xor := inst_xori | inst_xor
  es_alu_op_sll := inst_slli | inst_sll
  es_alu_op_srl := inst_srli | inst_srl
  es_alu_op_sra := inst_srai | inst_sra
  es_alu_op_lui := inst_lui



  when (es_src1_rs1 === 1.U) {
    es_alu.io.alu_src1 := reg_rdata_1
  } .elsewhen (es_src1_imm === 1.U) {
    es_alu.io.alu_src1 := inst_imm
  } .otherwise {
    es_alu.io.alu_src1 := 0.U;
  }

  when (es_src2_imm === 1.U) {
    es_alu.io.alu_src2 := inst_imm
  } .elsewhen (es_src2_pc === 1.U) {
    es_alu.io.alu_src2 := es_pc
  } .elsewhen (es_src2_rs2 === 1.U) {
    es_alu.io.alu_src2 := reg_rdata_2
  } .elsewhen (es_src2_rs2_self === 1.U) {
    es_alu.io.alu_src2 := rs2
  } .otherwise {
    es_alu.io.alu_src2 := 0.U
  }

  when (es_alu_op_add === 1.U) {
    es_alu.io.alu_op := 1.U
  } .elsewhen (es_alu_op_sub === 1.U) {
    es_alu.io.alu_op := 2.U
  } .elsewhen (es_alu_op_slt === 1.U) {
    es_alu.io.alu_op := 4.U
  } .elsewhen (es_alu_op_sltu === 1.U) {
    es_alu.io.alu_op := 8.U
  } .elsewhen (es_alu_op_and === 1.U) {
    es_alu.io.alu_op := 16.U
  } .elsewhen (es_alu_op_nor === 1.U) {
    es_alu.io.alu_op := 32.U
  } .elsewhen (es_alu_op_or === 1.U) {
    es_alu.io.alu_op := 64.U
  } .elsewhen (es_alu_op_xor === 1.U) {
    es_alu.io.alu_op := 128.U
  } .elsewhen (es_alu_op_sll === 1.U) {
    es_alu.io.alu_op := 256.U
  } .elsewhen (es_alu_op_srl === 1.U) {
    es_alu.io.alu_op := 512.U
  } .elsewhen (es_alu_op_sra === 1.U) {
    es_alu.io.alu_op := 1024.U
  } .elsewhen (es_alu_op_lui === 1.U) {
    es_alu.io.alu_op := 2048.U
  } .otherwise {
    es_alu.io.alu_op := 1.U
    // we do not use ALU in this case
  }
  
  alu_result := es_alu.io.alu_result
  alu_overflow := es_alu.io.alu_overflow
  

  // Write back related
  val reg_file = Module(new RegFile(2))
  val reg_wen = Wire(UInt(1.W))
  val reg_waddr = Wire(UInt(5.W))
  val reg_wdata = Wire(UInt(32.W))
  val reg_raddr_1 = Wire(UInt(5.W))
  val reg_raddr_2 = Wire(UInt(5.W))
  val reg_raddr = Wire(new regfile_raddr)
  val reg_rdata_1 = Wire(UInt(32.W))
  val reg_rdata_2 = Wire(UInt(32.W))
  val gr_we = Wire(UInt(1.W))

  reg_raddr.raddr1 := reg_raddr_1
  reg_raddr.raddr2 := reg_raddr_2

  // reg file connection
  reg_file.io.wen := reg_wen
  reg_file.io.waddr := reg_waddr
  reg_file.io.wdata := reg_wdata
  reg_file.io.raddr := reg_raddr.asUInt
  reg_rdata_1 := reg_file.io.rdata(0)
  reg_rdata_2 := reg_file.io.rdata(1)

  reg_waddr := rd
  
  // note that load instructions may only write when load data is returned
  when (es_load === 1.U) {
    reg_wen := 
  } .otherwise {
    reg_wen := gr_we
  }

  gr_we := inst_lui | inst_auipc | inst_jal | inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_addi | inst_slti | inst_sltiu | inst_xori | inst_ori | inst_andi | inst_slli | inst_srli | inst_srai | inst_add | inst_sub | inst_sll | inst_slt | inst_sltu | inst_xor | inst_srl | inst_sra | inst_or | inst_and
  

  // wdata related signals
  val reg_wdata_alu = Wire(UInt(1.W))
  val reg_wdata_mem = Wire(UInt(1.W))
  val reg_wdata_csr = Wire(UInt(1.W))

  // currently no write from csr
  reg_wdata_alu :=

  reg_wdata_mem := inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu


}