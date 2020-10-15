package Xim

import chisel3._


class CPU_IF extends Module {
    val io = IO(new Bundle {
        val inst_addr = Output(UInt(32.W))
        val inst_req_valid = Output(UInt(1.W))
        val inst_req_ack = Input(UInt(1.W))
        
        val inst_data = Input(UInt(32.W))
        val inst_valid = Input(UInt(1.W))
        val inst_ack = Output(UInt(1.W))
        
        val es_allowin = Input(UInt(1.W))
        val inst_reload = Input(UInt(1.W))
        // maybe we do not need to deal with reload in this pipeline
        val fs_to_es_valid = Output(UInt(1.W))
        val fs_pc = Output(UInt(32.W))
        val fs_inst = Output(UInt(32.W))
        val fs_ex = Output(UInt(1.W))
        val fs_excode = Output(UInt(5.W)) // maybe should be longer?
        
        val br_valid = Input(UInt(1.W))
        val br_target = Input(UInt(32.W))
        
        val ex_valid = Input(UInt(1.W))
        val ex_target = Input(UInt(32.W))
        
    })
    
    
    
    // unimplemented end
    
    val fs_valid = RegInit(0.U(1.W))
    val fs_allowin = Wire(UInt(1.W))
    val fs_ready_go = Wire(UInt(1.W))
    
    val next_pc = Wire(UInt(32.W))
    val next_fs_ex = RegInit(0.U(1.W))
    val fs_ex = RegInit(0.U(1.W))
    val fs_pc_r = RegInit((0x7ffffffcL).U(32.W))
    
    // some handy signals
    val addr_handshake = Wire(UInt(1.W))
    val data_handshake = Wire(UInt(1.W))
    val data_handshake_r = RegInit(0.U(1.W))
    
    val inst_req_valid_r = RegInit(0.U(1.W))
    val inst_ack_r = RegInit(0.U(1.W))
    val fs_inst_r = Reg(UInt(32.W))
    
    // Instruction misaligned has excode 0
    io.fs_excode := excode_const.InstructionMisaligned // the only possible exception here
    when (next_pc(1, 0) === 0.U) {
        // aligned
        next_fs_ex := 0.U
    } .otherwise {
        next_fs_ex := 1.U
    }
    
    fs_allowin := 1.U
    // TODO: check valid condition in the future
    when (fs_ready_go === 1.U && io.es_allowin === 1.U) {
        fs_valid := 0.U
    } .otherwise {
        fs_valid := 1.U
    }
    
    io.fs_to_es_valid := (fs_valid === 1.U && (data_handshake | data_handshake_r) === 1.U) || io.fs_ex === 1.U
    
    addr_handshake := io.inst_req_valid === 1.U && io.inst_req_ack === 1.U
    data_handshake := io.inst_ack === 1.U && io.inst_valid === 1.U
    // printf(p"io.inst_valid = ${io.inst_valid} fs_pc = ${io.fs_pc}\n")
    fs_ready_go := data_handshake | data_handshake_r | io.fs_ex
    // if we encounter an misaligned exception, we are ready to go
    
    io.fs_pc := fs_pc_r
    
    when(io.ex_valid === 1.U) {
        next_pc := io.ex_target
    }.elsewhen(io.br_valid === 1.U) {
        next_pc := io.br_target
    }.otherwise {
        next_pc := fs_pc_r + 4.U;
    }
    
    io.inst_addr := next_pc
    
    when(fs_ready_go === 1.U && io.es_allowin === 1.U) {
        // TODO: check maybe the update should happen when we are able to move to the next stage
        fs_pc_r := next_pc
        fs_ex := next_fs_ex
    }
    io.fs_ex := fs_ex
    
    when(io.ex_valid === 1.U) {
        data_handshake_r := 0.U
    }.elsewhen(data_handshake === 1.U && io.es_allowin === 1.U) {
        data_handshake_r := 0.U
    }.elsewhen(data_handshake === 1.U && io.es_allowin === 0.U) {
        data_handshake_r := 1.U
    }
    
    val inst_req_valid_set = RegInit(0.U(1.W))
    
    when (fs_ready_go === 1.U && io.es_allowin === 1.U && io.fs_ex === 0.U) {
        inst_req_valid_set := 0.U
    } .elsewhen (fs_valid === 1.U && inst_req_valid_set === 0.U) {
        inst_req_valid_set := 1.U
    }
    
    when (fs_valid === 1.U && inst_req_valid_set === 0.U) {
        inst_req_valid_r := 1.U;
    }.elsewhen(addr_handshake === 1.U) {
        inst_req_valid_r := 0.U
    }
    
    io.inst_req_valid := inst_req_valid_r // maybe we should consider some reload signals in the future
    
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
    
    when(data_handshake === 1.U) {
        // update our inst data
        fs_inst_r := io.inst_data
    }
    
    // printf("inst fetched in IF = %x addr_handshake = %d data_handshake = %d " +
    //  "branch_valid = %d branch target = %x\n", io.inst_data, addr_handshake, data_handshake, io.br_valid, io.br_target)
    
    
}

object CPU_IF extends App {
    chisel3.Driver.execute(args, () => new CPU_IF)
}
