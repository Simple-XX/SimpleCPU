# 一生一芯阶段性提交

## 陈国凯 mb-core

### 编译说明

除CPU核心外，在`cpu/`目录额外添加了一个AXIBridge.v文件，用于处理器内部信号转换。该文件已经添加到Makefile的file list中。

### 当前进展

当前已经完成全部访存信号的连接，没有使用frontend的AXI端口及MTIP、MEIP端口，可在vcs仿真环境下输出HHHHH。处理器支持RV64IZicsr。