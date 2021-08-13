package Xim

import chisel3._

object priv_consts extends PriviledgeLevelConstants

class PriviledgeReg extends Module {
    val io = IO(new Bundle {
        val wen = Input(Bool())
        val wlevel = Input(UInt(2.W))
        val rlevel = Output(UInt(2.W))
    })
    
    val priviledge_level = RegInit(3.U(2.W)) //initialized as Machine level
    
    when(io.wen) {
        priviledge_level := io.wlevel
    }

    io.rlevel := priviledge_level
}

class PriviledgeSignal extends Module {
    val io = IO(new Bundle {
        val es_valid = Input(UInt(1.W))
        val es_ex_work = Input(UInt(1.W))
        val mret_work = Input(UInt(1.W))
        val sret_work = Input(UInt(1.W))

        val mstatus_mpp = Input(UInt(2.W))
        val sstatus_spp = Input(UInt(1.W))

        val priv_level = Output(UInt(2.W))

        val deleg_trap = Input(Bool())
    })

    //priviledge level
    val priv_level = Module(new PriviledgeReg)
    val priv_wen = Wire(Bool())
    val priv_wlevel = Wire(UInt(2.W))
    val priv_rlevel = Wire(UInt(2.W))
    val next_priv_level = Wire(UInt(2.W))

    priv_level.io.wen := priv_wen
    priv_level.io.wlevel := priv_wlevel
    priv_rlevel := priv_level.io.rlevel
    priv_wlevel := next_priv_level

    io.priv_level := priv_rlevel

    when (io.es_valid === 1.U && io.mret_work === 1.U && priv_rlevel === priv_consts.Machine) {
        next_priv_level := io.mstatus_mpp
    } .elsewhen (io.es_valid === 1.U && io.sret_work === 1.U && priv_rlevel === priv_consts.Supervisor) {
        next_priv_level := io.sstatus_spp
    } .elsewhen (io.es_ex_work === 1.U) {
        when (io.deleg_trap === true.B) {
            next_priv_level := priv_consts.Supervisor
        } .otherwise {
            next_priv_level := priv_consts.Machine
        }
    } .otherwise {
        next_priv_level := priv_consts.Machine
    }

    when (io.es_valid === 1.U && (io.mret_work === 1.U || io.sret_work === 1.U)) {
        priv_wen := true.B
    } .elsewhen (io.es_ex_work === 1.U) {
        priv_wen := true.B
    } .otherwise {
        priv_wen := false.B
    }
}