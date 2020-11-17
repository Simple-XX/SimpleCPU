# Implementation details

## Unimplemented features:

1. Supervisor and User priviledge level (along with `medeleg` and `mideleg` CSRs).

2. Writable `misa`, `mvendorid`, `marchid`, `mimpid`, `mhartid` CSRs

3. Vectored trap handling (`mtvec` MODE is hardwired to zero)

4. Page and fetch fault related exceptions (since we do not have a virtual memory system for now)

5. External exceptions (unable to determine the currect way of handling this)

6. Software exceptions (I am lazy enough)

7. break exceptions (No need if you do not need to debug)

8. MMU (WIP but not tested)

## Non-standard features:

1. CSR-mapped `mtime` and `mtimecmp`

We use 0x7c0 as `mtime`, 0x7c2 as `mtimecmp`


## Implementation Documentations

### CSR related

CSRRW ALU: rs1 + 0-> CSR CSR->rd
CSRRS ALU: rs1 or CSR -> CSR CSR->rd
CSRRC ALU: ~rs1 and CSR -> CSR CSR->rd

CSRRWI: ALU: imm + 0 -> CSR CSR->rd
CSRRSI: ALU: imm or CSR -> CSR CSR->rd
CSRRCI: ALU: ~imm and CSR -> CSR CSR->rd

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

#### Design Notes

We currently replace an instruction following the branch taken with a NOP-like instruction.
This is fine with the normal user instructions already. While for an exception related condition, it may cause some
weird conditions like mepc is written with an unexpected instruction.
For our naive design, an instruction follwing the branch will not trigger any exception itself 
so we do not handle exceptions. For the external exceptions, we will handle them when we are out of this condition

### Virtual Memory Ralated

As long as there are no tlb related definiations in RISC-V spec, I will design my own tlb format for internal usage.

#### TLB entry format

**sv39 mode only**

| page size |  VPN |  PPN   | DAGUXWRV |
| --------- | ---- | ------ | -------- |
|    2      | 27   | 44     |    8     |

##### page size

0: 4KB record
1: 2MB record
2: 1GB record
3: reserved

##### DAGUXWRY

Not implemented for now

##### Translation process:
We divide VPN into three part: VPN2(9) VPN1(9) VPN0(9), PPN into three part: PPN2(26) PPN1(9) PPN0(9).

Vaddr(11, 0) -> always keep untouched

**4KB record**:Vaddr(38, 12) -> throw to match, Paddr: PPN2 || PPN1 || PPN(0) || Vaddr(11, 0)

**2MB record**:Vaddr(38, 21) -> throw to match, Vaddr(20, 12) keep untouched, Paddr: PPN2 || PPN1 || Vaddr(20, 0)

**1GB record**:Vaddr(38, 30) -> throw to match, Vaddr(29, 12) keep untouched, Paddr: PPN2 || Vaddr(29, 0)

TLB retire algorithm: loop queue


### W suffix instructions in RV64I

Note that we cannot always simply pass 64 bit srcs to ALU and expect the lower word satisfy the W suffix requirement.

ADDIW: ok -> currently implemented as no W
SLLIW: ok -> currently implemented as no W
SRLIW: NOT OK (higher bits may come in) -> implemented
SRAIW: NOT OK (higher bits affect sig) -> implemented
ADDW: ok -> currently implemented as no W
SUBW: ok -> currently implemented as no W
SLLW: ok -> currently implemented as no W
SRLW: NOT OK (higher bits may come in) -> implemented
SRAW: NOT OK (higher bits affect sig) -> implemented

### Memory related design

Inst and data sram-like => MMIO ? AXI_MEM : AXI_MMIO

todo: add a cross bar for PTW