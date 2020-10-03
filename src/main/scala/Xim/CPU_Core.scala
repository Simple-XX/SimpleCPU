
package Xim

import chisel3._

class CPU_Core extends Module {
    val io = IO(new Bundle {
        val inst_addr        = Output(UInt(32.W))
        val inst_req_valid   = Output(UInt(1.W))
        val inst_req_ack     = Input(UInt(1.W))
        
        val inst_data        = Input(UInt(32.W))
        val inst_valid       = Input(UInt(1.W))
        val inst_ack         = Output(UInt(1.W))
        
        val data_addr        = Output(UInt(32.W))
        val data_write       = Output(UInt(1.W))
        val data_read        = Output(UInt(1.W))
        val data_size        = Output(UInt(2.W))
        
        val data_write_data  = Output(UInt(32.W))
        
        val data_req_ack      = Input(UInt(1.W))
        
        val data_read_data   = Input(UInt(32.W))
        val data_read_valid  = Input(UInt(1.W))
        val data_data_ack    = Output(UInt(1.W))
        
        // for debug purpose
        val reg_wen          = Output(UInt(1.W))
        val reg_wdata        = Output(UInt(32.W))
        val reg_waddr        = Output(UInt(5.W))
    })
    val IF_Stage = Module(new CPU_IF)
    io.inst_addr := IF_Stage.io.inst_addr
    io.inst_req_valid := IF_Stage.io.inst_req_valid
    IF_Stage.io.inst_req_ack := io.inst_req_ack
    IF_Stage.io.inst_data := io.inst_data
    IF_Stage.io.inst_valid := io.inst_valid
    io.inst_ack := IF_Stage.io.inst_ack
    
    val EX_Stage = Module(new CPU_EX)
    io.data_addr := EX_Stage.io.data_addr
    io.data_write := EX_Stage.io.data_write
    io.data_read := EX_Stage.io.data_read
    io.data_size := EX_Stage.io.data_size
    io.data_write_data := EX_Stage.io.data_write_data
    EX_Stage.io.data_req_ack := io.data_req_ack
    EX_Stage.io.data_read_data := io.data_read_data
    EX_Stage.io.data_read_valid := io.data_read_valid
    io.data_data_ack := EX_Stage.io.data_data_ack
    // debug part
    io.reg_waddr := EX_Stage.io.es_reg_waddr
    io.reg_wdata := EX_Stage.io.es_reg_wdata
    io.reg_wen := EX_Stage.io.es_reg_wen
    
    EX_Stage.io.fs_pc := IF_Stage.io.fs_pc
    EX_Stage.io.fs_inst := IF_Stage.io.fs_inst
    EX_Stage.io.fs_ex := IF_Stage.io.fs_ex
    EX_Stage.io.fs_excode := IF_Stage.io.fs_excode
    EX_Stage.io.fs_to_es_valid := IF_Stage.io.fs_to_es_valid
    
    IF_Stage.io.inst_reload := EX_Stage.io.inst_reload
    IF_Stage.io.es_allowin := EX_Stage.io.es_allowin
    IF_Stage.io.br_valid := EX_Stage.io.br_valid
    IF_Stage.io.br_target := EX_Stage.io.br_target
    IF_Stage.io.ex_valid := EX_Stage.io.ex_valid
    IF_Stage.io.ex_target := EX_Stage.io.ex_target
    
}

object CPU_Core extends App {
    chisel3.Driver.execute(args, () => new CPU_Core)
}