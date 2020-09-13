# 一生一芯计划

MB estas mia blato en Esperanto
MB is short for my chip in Esperanto

## 目标

使用chisel实现一支持risc-v 64 IMZicsr指令集的CPU，支持异常和Machine态，可运行RT-Thread系统。

### 当前目标

考虑到当前时间有限，经调研，启动RT-Thread最少需求为risc-v 32 IZicsr并支持异常和Machine态。

## 整体结构

预计最终为多级流水带Cache结构，当前设计为二级流水线（取指&执行）。

### 当前设计

IF级发出指令请求后等待握手，等待请求握手完成后完成数据握手，方可进入下一流水级。
EX级进行指令译码执行访存写回，若为访存指令则发出访存请求，请求握手完成后完成数据握手。注意一旦进入就可计算出分支发生与否及相应地址。若发生分支跳转则前递给上一流水级，提供下一pc值并将本次数据清空。