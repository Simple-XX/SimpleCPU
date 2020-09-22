
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
    val data_read         = Output(UInt(1.W))

    val data_write_data  = Output(UInt(32.W))
    val data_wstrb       = Output(UInt(4.W))

    val data_req_ack      = Input(UInt(1.W))

    val data_read_data   = Input(UInt(32.W))
    val data_read_valid  = Input(UInt(1.W))
    val data_data_ack    = Output(UInt(1.W))
  })
  // TODO: add pipeline here
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
  io.data_write_data := EX_Stage.io.data_write_data
  io.data_wstrb := EX_Stage.io.data_wstrb
  EX_Stage.io.data_req_ack := io.data_req_ack
  EX_Stage.io.data_read_data := io.data_read_data
  EX_Stage.io.data_read_valid := io.data_read_valid
  io.data_data_ack := EX_Stage.io.data_data_ack

}

object CPU_Core extends App {
  chisel3.Driver.execute(args, () => new CPU_Core)
}