package Xim

import chisel3._

object SoC_Main extends App {
    iotesters.Driver.execute(args, () => new SoC) {
        c => new SoC_UnitTester(c)
    }
    /*
    iotesters.Driver.execute(args, () => new CPU_Core) {
        c => new CPU_Core_Arithmetic_UnitTester(c)
    }
     */
}
