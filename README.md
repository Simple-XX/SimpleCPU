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
EX级进行指令译码执行访存写回，若为访存指令则发出访存请求，请求握手完成后完成数据握手。注意一旦进入就可计算出分支发生与否及相应地址。若发生分支跳转则前递给上一流水级，提供下一pc值并设置本级reload信号，使得下一指令不产生实际效果。

## 进度日志：

### Finished

2020.9.22 第一条指令(addi x1, zero, 100)

2020.9.24 分支指令（beq zero, zero, 0x10）

2020.9.26 AXI RAM接入，构建简易SoC

2020.10.2 CSR指令接入

2020.10.3 开跑部分功能测试

### TODO

1. load and store (the instructions that need to stay multiple cycles in ES)
2. CSR integration (instructions included)
3. ex related
4. test framework

### Notes

实现测试框架的一点思路：

程序正常执行甚至发生异常时，几乎不会出现前后两条指令PC值一致的情形，如此一来判定指令前进变得异常简单，只需观察周期前后PC值是否有所改变。

可以为原来的多周期处理器编写类似的verilog testbench导出trace与本处理器比较。