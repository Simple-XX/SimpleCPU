# Implementation details

## Unimplemented features:

1. Supervisor and User priviledge level (along with `medeleg` and `mideleg` CSRs).

2. Writable `misa`, `mvendorid`, `marchid`, `mimpid`, `mhartid` CSRs

3. Vectored trap handling (`mtvec` MODE is hardwired to zero)

4. Page and fetch fault related exceptions (since we do not have a virtual memory system for now)

5. External exceptions (unable to determine the currect way of handling this)

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