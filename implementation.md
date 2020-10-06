# Implementation details

## Unimplemented features:

1. Supervisor and User priviledge level (along with `medeleg` and `mideleg` CSRs).

2. Writable `misa`, `mvendorid`, `marchid`, `mimpid`, `mhartid` CSRs

3. Vectored trap handling (`mtvec` MODE is hardwired to zero)

4. Page and fetch fault related exceptions (since we do not have a virtual memory system for now)

5. External exceptions (unable to determine the currect way of handling this)

6. Software exceptions (I am lazy enough)

7. break exceptions (No need if you do not need to debug)

## Non-standard features:

1. CSR-mapped `mtime` and `mtimecmp`

We use 0x7c0 as `mtimelo`, 0x7c1 as `mtimehi`, 0x7c2 as `mtimecmplo`, 0x7c3 as `mtimecmphi`


## Implementation Documentations

### CSR related

CSRRW ALU: rs1 + 0-> CSR CSR->rd
CSRRS ALU: rs1 or CSR -> CSR CSR->rd
CSRRC ALU: rs1 xor CSR -> CSR CSR->rd

CSRRWI: ALU: imm + 0 -> CSR CSR->rd
CSRRSI: ALU: imm or CSR -> CSR CSR->rd
CSRRCI: ALU: imm xor CSR -> CSR CSR->rd

### Exception related

#### Complete supported exception list

1. Machine timer interrupt
2. Instruction address misaligned
3. Illegal instruction
4. Load address misaligned
5. Store address misaligned
6. Environmental call from M mode

#### IF identified exceptions:

1. Instruction address misaligned

#### EX indentified exceptions:

1. Machine timer interrupt
2. Illegal instruction
3. Load address misaligned
4. Store address misaligned
5. Environmental call from M mode