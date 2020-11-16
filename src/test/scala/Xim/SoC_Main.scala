package Xim

import chisel3._
import firrtl.stage.RunFirrtlTransformAnnotation

object SoC_Main_Type_One extends App {
    iotesters.Driver.execute(args, () => new SoC) {
        c => new SoC_UnitTester_Type1(c)
    }
}

object SoC_Main_Type_Two extends App {
    iotesters.Driver.execute(args, () => new SoC) {
        c => new SoC_UnitTester_Type2(c)
    }
}

object SoC_Main_Type_Three extends App {
    iotesters.Driver.execute(args, () => new SoC) {
        c => new SoC_UnitTester_Type3(c)
    }
}
