package codegen.Instructions;

import cfg.Optimization.RegisterAllocation.REG;

/**
 * @author Nicola
 */
public class Register extends Location {
        
    private static enum Registers {
        rax,
        rbx,
        rcx,
        rdx,
        rsp,
        rbp,
        rsi,
        rdi,
        r8,
        r9,
        r10, 
        r11,
        r12,
        r13,
        r14,
        r15
    }
    
    private final Registers regName;
    
    private Register(Registers regName) {
        this.regName = regName;
        this.size = 8;
    }
    
    public Register(REG reg, int size) {
        this.size = size;
        for (Registers testReg : Registers.values()) {
            if (testReg.toString().equals(reg.toString())) {
                this.regName = testReg;
                return;
            }
        }
        throw new Error("Cannot find corresponding register");
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    private String toLow(Registers reg) {
        switch (reg) {
        case r10:
            return "r10b";
        case r11:
            return "r11b";
        case r12:
            return "r12b";
        case r13:
            return "r13b";
        case r14:
            return "r14b";
        case r15:
            return "r15b";
        case r8:
            return "r8b";
        case r9:
            return "r9b";
        case rax:
            return "al";
        case rbp:
            return "bpl";
        case rbx:
            return "bl";
        case rcx:
            return "cl";
        case rdi:
            return "dil";
        case rdx:
            return "dl";
        case rsi:
            return "sil";
        case rsp:
            return "spl";
        default:
            throw new Error("unexpected");
        }
    }
    
    @Override
    public String toCode() {
        if (size == 8) {
            return "%" + regName.name();
        } else {
            return "%" + toLow(regName);
        }
    }
    
    @Override
    public boolean isReg() {
        return true;
    }
    
    // Producers
    public static Register rax() {
        return new Register(Registers.rax);
    }
    
    public static Register rbx() {
        return new Register(Registers.rbx);
    }
    
    public static Register rcx() {
        return new Register(Registers.rcx);
    }
    
    public static Register rdx() {
        return new Register(Registers.rdx);
    }
    
    public static Register rsp() {
        return new Register(Registers.rsp);
    }
    
    public static Register rbp() {
        return new Register(Registers.rbp);
    }
    
    public static Register rsi() {
        return new Register(Registers.rsi);
    }
    
    public static Register rdi() {
        return new Register(Registers.rdi);
    }
    
    public static Register r8() {
        return new Register(Registers.r8);
    }
    
    public static Register r9() {
        return new Register(Registers.r9);
    }
    
    public static Register r10() {
        return new Register(Registers.r10);
    }
    
    public static Register r11() {
        return new Register(Registers.r11);
    }
    
    public static Register r12() {
        return new Register(Registers.r12);
    }
    
    public static Register r13() {
        return new Register(Registers.r13);
    }
    
    public static Register r14() {
        return new Register(Registers.r14);
    }
    
    public static Register r15() {
        return new Register(Registers.r15);
    }
    
    @Override
    public boolean equals(Object thatObj) {
        if (!(thatObj instanceof Register)) {
            return false;
        }
        Register that = (Register) thatObj;
        return this.regName.equals(that.regName);
    }
    
}
