package Xim

import chisel3._

object CPU_Core_Main extends App {
    iotesters.Driver.execute(args, () => new CPU_Core) {
        c => new CPU_Core_UnitTester(c)
    }
}
