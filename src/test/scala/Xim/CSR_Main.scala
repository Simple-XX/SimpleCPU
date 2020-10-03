package Xim

import chisel3._

object CSR_Main extends App {
    iotesters.Driver.execute(args, () => new CSR) {
        c => new CSR_UnitTester(c)
    }
}