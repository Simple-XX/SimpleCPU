import os
import re # regular expression

path = "/home/wangns/riscv-tests/isa/" #the path of compiled risc-v test

files= os.listdir(path) # get all file names

ans_files = []
ans_list = []

for file in files:
    if os.path.isdir(path + file): # remove dirs
        continue
    if  re.match('.*dump', file) != None:
        continue
    #if  re.match('rv64ui-p-', file) != None:
    if re.match('rv64si-p-supervisor', file) != None:
        ans_files.append(file)

# print(ans_files)
i = 1

for file in ans_files:
    print("************ New Test of " + file + " ************")
    print(i, end = '')
    i = i + 1
    print("th test")
    os.system('riscv64-unknown-elf-objcopy -O binary ' + path + file + ' /home/wangns/test.bin.ori')
    os.system('riscv64-unknown-elf-objcopy -I binary -O binary --reverse-bytes=8 /home/wangns/test.bin.ori /home/wangns/test.bin')
    os.system('sbt "test:runMain Xim.SoC_Main_Type_Two --backend-name verilator"')
