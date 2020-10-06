import os
import re # regular expression

path = "/Users/cgk/ownCloud/课程/一生一芯/ict/riscv-tests/isa/" #the path of compiled risc-v test

files= os.listdir(path) # get all file names

ans_files = []
ans_list = []

for file in files:
    if os.path.isdir(path + file): # remove dirs
        continue
    if  re.match('.*dump', file) != None:
        continue
    if  re.match('rv32ui-p-*', file) != None:
        ans_files.append(file)

# print(ans_files)
i = 1

for file in ans_files:
    print("************ New Test of " + file + " ************")
    print(i, end = '')
    i = i + 1
    print("th test")
    os.system('/Users/cgk/ownCloud/课程/一生一芯/ict/riscv64-unknown-elf-gcc-8.3.0-2020.04.0-x86_64-apple-darwin/bin/riscv64-unknown-elf-objcopy -O binary ' + path + file + ' /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin.ori')
    os.system('/Users/cgk/ownCloud/课程/一生一芯/ict/riscv64-unknown-elf-gcc-8.3.0-2020.04.0-x86_64-apple-darwin/bin/riscv64-unknown-elf-objcopy -I binary -O binary --reverse-bytes=4 /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin.ori /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin')
    os.system('sbt "test:runMain Xim.SoC_Main --backend-name verilator"')
