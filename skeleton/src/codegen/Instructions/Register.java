package codegen.Instructions;

/**
 * @author Nicola
 */
public class Register extends Exp {
        
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
    }
       
    @Override
    public String toString() {
        return regName.name();
    }
    
    @Override
    public String toCode() {
        return "%" + regName.name();
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
    
}
