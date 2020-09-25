package Xim

import chisel3._
import javafx.scene.input.InputMethodTextRun


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
        // maybe we do not need to deal with reload in this pipeline
        val fs_to_es_valid   = Output(UInt(1.W))
        val fs_pc            = Output(UInt(32.W))
        val fs_inst          = Output(UInt(32.W))
        val fs_ex            = Output(UInt(1.W))
        val fs_excode        = Output(UInt(5.W)) // maybe should be longer?
        
        val br_valid         = Input(UInt(1.W))
        val br_target        = Input(UInt(32.W))
        
        val ex_valid         = Input(UInt(1.W))
        val ex_target        = Input(UInt(32.W))
        
    })
    // Currently not implemented signals
    io.fs_excode := 0.U
    io.fs_ex := 0.U
    
    // unimplemented end
    
    val fs_valid = Reg(UInt(1.W))
    val fs_allowin = Wire(UInt(1.W))
    val fs_ready_go = Wire(UInt(1.W))
    
    val next_pc = Wire(UInt(32.W))
    val fs_pc_r = RegInit((0xfffffffcL).U(32.W))
    
    // some handy signals
    val addr_handshake = Wire(UInt(1.W))
    val data_handshake = Wire(UInt(1.W))
    val data_handshake_r = RegInit(0.U(1.W))
    
    val inst_req_valid_r = RegInit(0.U(1.W))
    val inst_ack_r = RegInit(0.U(1.W))
    val fs_inst_r = Reg(UInt(32.W))
    
    fs_allowin := 1.U
    // TODO: check valid condition in the future
    fs_valid := 1.U
    io.fs_to_es_valid := fs_valid === 1.U && (data_handshake === 1.U || data_handshake_r === 1.U)
    
    addr_handshake := io.inst_req_valid === 1.U && io.inst_req_ack === 1.U
    data_handshake := io.inst_ack === 1.U && io.inst_valid === 1.U
    printf(p"io.inst_valid = ${io.inst_valid} fs_pc = ${io.fs_pc}\n")
    fs_ready_go := data_handshake
    
    io.fs_pc := fs_pc_r
    
    when (io.ex_valid === 1.U) {
        next_pc := io.ex_target
    } .elsewhen (io.br_valid === 1.U) {
        next_pc := io.br_target
    } .otherwise {
        next_pc := fs_pc_r + 4.U;
    }
    
    io.inst_addr := next_pc
    
    when (fs_ready_go === 1.U) {
        // TODO: check maybe the update should happen when we are able to move to the next stage
        fs_pc_r := next_pc
    }
    
    when (io.ex_valid === 1.U) {
        data_handshake_r := 0.U
    } .elsewhen (data_handshake === 1.U && io.es_allowin === 1.U) {
        data_handshake_r := 0.U
    } .elsewhen (data_handshake === 1.U && io.es_allowin === 0.U) {
        data_handshake_r := 1.U
    }
    
    
    when ((io.es_allowin === 1.U) && !(io.fs_ex === 1.U)) {
        inst_req_valid_r := 1.U;
    } .elsewhen (addr_handshake === 1.U) {
        inst_req_valid_r := 0.U
    }
    
    io.inst_req_valid :=  inst_req_valid_r // maybe we should consider some reload signals in the future
    
    /*
    io.inst_ack := inst_ack_r;
  
    when (addr_handshake === 1.U) {
      // handshake done
      inst_ack_r := 1.U
    } .elsewhen (data_handshake === 1.U) {
      inst_ack_r := 0.U
    }
     */
    io.inst_ack := 1.U // always acknowledge
    
    io.fs_inst := fs_inst_r
    
    when (data_handshake === 1.U) {
        // update our inst data
        fs_inst_r := io.inst_data
    }
    
    
    printf("inst fetched in IF = %x data_handshake = %d " +
      "branch_valid = %d branch target = %x\n",io.inst_data, data_handshake, io.br_valid, io.br_target)
    
    
    
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
        
        val fs_to_es_valid   = Input(UInt(1.W))
        val fs_pc            = Input(UInt(32.W))
        val fs_inst          = Input(UInt(32.W))
        val fs_ex            = Input(UInt(1.W))
        val fs_excode        = Input(UInt(5.W))
        
        val br_valid         = Output(UInt(1.W))
        val br_target        = Output(UInt(32.W))
        val ex_valid         = Output(UInt(1.W))
        val ex_target        = Output(UInt(32.W))
    
        // for debug
        val es_reg_wen           = Output(UInt(1.W))
        val es_reg_waddr     = Output(UInt(5.W))
        val es_reg_wdata     = Output(UInt(32.W))
    })
    
    printf(p"reg_wen = ${io.es_reg_wen} reg_waddr = ${io.es_reg_waddr} reg_wdata = ${io.es_reg_wdata}\n")
    
    val es_valid = RegInit(0.U(1.W))
    // es_allowin as Output
    val es_ready_go = Wire(UInt(1.W))
    val es_pc = Reg(UInt(32.W))
    val es_instr = Reg(UInt(32.W))
    
    // branch related
    val rs1_equal_rs2 = Wire(UInt(1.W))
    val rs1_less_rs2_unsigned = Wire(UInt(1.W))
    val rs1_less_rs2_signed = Wire(UInt(1.W))
    val br_taken = Wire(UInt(1.W))
    val inst_reload = Wire(UInt(1.W))
    val beq_taken = Wire(UInt(1.W))
    val bne_taken = Wire(UInt(1.W))
    val blt_taken = Wire(UInt(1.W))
    val bge_taken = Wire(UInt(1.W))
    val bltu_taken = Wire(UInt(1.W))
    val bgeu_taken = Wire(UInt(1.W))
    val inst_reload_r = RegInit(0.U(1.W))
    
    
    
    // decode related
    val es_load = Wire(UInt(1.W))
    val es_store = Wire(UInt(1.W))
    val es_branch = Wire(UInt(1.W))
    val es_load_r = RegInit(0.U(1.W))
    val es_store_r = RegInit(1.U(1.W))
    // branch and jump is finished in a cycle
    
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
    val inst_i = Wire(UInt(1.W))
    val inst_s = Wire(UInt(1.W))
    val inst_b = Wire(UInt(1.W))
    val inst_u = Wire(UInt(1.W))
    val inst_j = Wire(UInt(1.W))
    val inst_r = Wire(UInt(1.W))
    
    inst_i := inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_addi | inst_slti | inst_sltiu | inst_xori | inst_ori | inst_andi | inst_fence_i
    inst_s := inst_sb | inst_sh | inst_sw
    inst_b := inst_beq | inst_bne | inst_blt | inst_bge | inst_bltu | inst_bgeu
    inst_u := inst_lui | inst_auipc
    inst_j := inst_jal
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
    printf(p"io.es_allowin = ${io.es_allowin} io.fs_to_es_valid = ${io.fs_to_es_valid}\n")
    printf("es_instr = %x\n", es_instr)
    
    val es_new_instr = Wire(UInt(1.W))
    
    es_new_instr := io.es_allowin === 1.U && io.fs_to_es_valid === 1.U
    
    when (es_new_instr === 1.U && inst_reload === 1.U) {
        es_instr := 0x00000033.U // provide a nop instruction (add zero, zero, zero)
    } .elsewhen (es_new_instr === 1.U) {
        es_instr := io.fs_inst
    }
    
    when (inst_reload === 1.U) {
        es_valid := 0.U
    } .elsewhen (io.es_allowin === 1.U) {
        es_valid := io.fs_to_es_valid
        // TODO: check exception conditions here
    }
    
    when (es_new_instr === 1.U) {
        es_pc := io.fs_pc
    }
    
    printf(p"es_pc = ${es_pc}\n")
    
    opcode := es_instr(6, 0)
    funct7 := es_instr(31, 25)
    funct3 := es_instr(14, 12)
    rs1 := es_instr(19, 15)
    rs2 := es_instr(24, 20)
    rd := es_instr(11, 7)
    I_imm := (I_imm_b.asUInt).asSInt()
    S_imm := (S_imm_b.asUInt).asSInt()
    B_imm := (B_imm_b.asUInt).asSInt()
    U_imm := U_imm_b.asUInt
    J_imm := (J_imm_b.asUInt).asSInt()
    
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
    
    U_imm_b.inst_31 := es_instr(31)
    U_imm_b.inst_30_20 := es_instr(30, 20)
    U_imm_b.inst_19_12 := es_instr(19, 12)
    U_imm_b.zero := 0.U
    
    J_imm_b.inst_31 := es_instr(31)
    J_imm_b.inst_19_12 := es_instr(19, 12)
    J_imm_b.inst_20 := es_instr(20)
    J_imm_b.inst_30_25 := es_instr(30, 25)
    J_imm_b.inst_24_21 := es_instr(24, 21)
    J_imm_b.zero := 0.U
    
    
    
    val opcode_decoder: decoder_7_128 = Module(new decoder_7_128)
    val funct7_decoder: decoder_7_128 = Module(new decoder_7_128)
    val funct3_decoder: decoder_3_8 = Module(new decoder_3_8)
    
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
    // val alu_op = Wire(UInt(12.W))
    val es_src1_rs1 = Wire(UInt(1.W))
    val es_src1_imm = Wire(UInt(1.W))
    val es_src2_rs2 = Wire(UInt(1.W))
    val es_src2_rs2_self = Wire(UInt(1.W))
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
    
    // Write back related
    val reg_file = Module(new RegFile(2))
    val reg_wen = Wire(UInt(1.W))
    val reg_waddr = Wire(UInt(5.W))
    val reg_wdata = Wire(UInt(32.W))
    val reg_raddr_1 = Wire(UInt(5.W))
    val reg_raddr_2 = Wire(UInt(5.W))
    // val reg_raddr = Wire(new regfile_raddr)
    val reg_rdata_1 = Wire(UInt(32.W))
    val reg_rdata_2 = Wire(UInt(32.W))
    val gr_we = Wire(UInt(1.W))
    
    io.es_reg_wen := reg_wen
    io.es_reg_waddr := reg_waddr
    io.es_reg_wdata := reg_wdata
    
    reg_raddr_1 := rs1
    reg_raddr_2 := rs2
    
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
    
    io.data_addr := alu_result
    
    // Data load and store related
    es_load := inst_lw | inst_lh | inst_lhu | inst_lb | inst_lbu
    es_store := inst_sw | inst_sh | inst_sb
    es_branch := inst_b | inst_jal | inst_jalr // jump instruction is dealed with the same schema
    
    when (es_new_instr === 1.U) {
        es_load_r := es_load
    }
    
    when (es_new_instr === 1.U) {
        es_store_r := es_store
    }
    
    // branch related
    
    rs1_equal_rs2 := reg_rdata_1 === reg_rdata_2
    rs1_less_rs2_unsigned := reg_rdata_1.asUInt() < reg_rdata_2.asUInt()
    rs1_less_rs2_signed := reg_rdata_1.asSInt() < reg_rdata_2.asSInt()
    
    beq_taken := inst_beq === 1.U && rs1_equal_rs2 === 1.U
    bne_taken := inst_bne === 1.U &&  rs1_equal_rs2 === 0.U
    blt_taken := inst_blt === 1.U && rs1_less_rs2_signed === 1.U
    bge_taken := inst_bge === 1.U && rs1_less_rs2_signed === 0.U
    bltu_taken := inst_bltu === 1.U && rs1_less_rs2_unsigned === 1.U
    bgeu_taken := inst_bgeu === 1.U && rs1_less_rs2_unsigned === 0.U
    
    br_taken := beq_taken | bne_taken | blt_taken | bge_taken | bltu_taken | bgeu_taken
    
    inst_reload := br_taken | inst_jal | inst_jalr
    io.br_valid := inst_reload
    
    when (br_taken === 1.U || inst_jal === 1.U | inst_jalr === 1.U) {
        io.br_target := alu_result
        printf(p"Branch taken, target = ${io.br_target}")
    } .otherwise {
        io.br_target := 0.U
    }
    
    when (es_new_instr === 1.U) {
        inst_reload_r := inst_reload // only as a debug method
        printf(p"New instr comes, reload: ${inst_reload_r}\n")
    }
    
    io.inst_reload := inst_reload_r
    
    // reg file connection
    reg_file.io.wen := reg_wen
    reg_file.io.waddr := reg_waddr
    reg_file.io.wdata := reg_wdata
    reg_file.io.raddr(0) := reg_raddr_1
    reg_file.io.raddr(1) := reg_raddr_2
    reg_rdata_1 := reg_file.io.rdata(0)
    reg_rdata_2 := reg_file.io.rdata(1)
    
    reg_waddr := rd
    
    // note that load instructions may only write when load data is returned
    when (es_load === 1.U) {
        // TODO: revise proper condition here
        // The current condition indicates that if our instruction is not a load, it should go directly into the write
        // back stage, which finishes in a single cycle
        reg_wen := 1.U
    } .otherwise {
        reg_wen := gr_we
    }
    
    gr_we := inst_lui | inst_auipc | inst_jal | inst_jalr | inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu | inst_addi |
      inst_slti | inst_sltiu | inst_xori | inst_ori | inst_andi | inst_slli | inst_srli | inst_srai | inst_add |
      inst_sub | inst_sll | inst_slt | inst_sltu | inst_xor | inst_srl | inst_sra | inst_or | inst_and | inst_csrrc |
      inst_csrrci | inst_csrrs | inst_csrrsi | inst_csrrw | inst_csrrwi
    
    // wdata related signals
    val reg_wdata_alu = Wire(UInt(1.W))
    val reg_wdata_mem = Wire(UInt(1.W))
    val reg_wdata_csr = Wire(UInt(1.W))
    
    // currently no write from csr
    reg_wdata_alu := inst_lui | inst_auipc | inst_jal | inst_jalr | inst_addi | inst_slti | inst_sltiu | inst_xori |
      inst_ori | inst_andi | inst_slli | inst_srli | inst_srai | inst_add | inst_sub | inst_sll | inst_slt | inst_sltu |
      inst_xor | inst_srl | inst_sra | inst_or | inst_and
    
    reg_wdata_mem := inst_lb | inst_lh | inst_lw | inst_lbu | inst_lhu
    
    reg_wdata_csr := inst_csrrc | inst_csrrci | inst_csrrs | inst_csrrsi | inst_csrrw | inst_csrrwi
    
    when (reg_wdata_alu === 1.U) {
        reg_wdata := alu_result
    } .elsewhen (reg_wdata_mem === 1.U) {
        // TODO: check the proper condition here: stored data rather than wired ones
        reg_wdata := io.data_read_data
    } .elsewhen (reg_wdata_csr === 1.U) {
        // TODO: check the proper condition here after adding csrs
        reg_wdata := 0.U
    } .otherwise {
        reg_wdata := 0.U
    }
    
    
    /*
    * Currently not implemented signals
    * */
    // io.inst_reload := 0.U
    io.ex_valid := 0.U
    io.data_wstrb := 0xf.U
    // es_store := 0.U
    // es_load := 0.U
    //io.br_target := 0.U
    // es_branch := 0.U
    io.data_write_data := 0.U
    //io.br_valid := 0.U
    io.data_write := 0.U
    io.data_read := 0.U
    io.ex_target := 0.U
    
    io.data_data_ack := 0.U
}

object CPU_IF extends App {
    chisel3.Driver.execute(args, () => new CPU_IF)
}

object CPU_EX extends App {
    chisel3.Driver.execute(args, () => new CPU_EX)
}