package codegen;

/**
 * @author Nicola
 */

public class Register extends Exp {
        
    public static enum Registers {
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
    
    public Register(Registers regName) {
        this.regName = regName;
    }
    
    @Override
    public String toString() {
        return regName.name();
    }
    
    @Override
    public String toCode() {
        //TODO: implement conversion to assembly
        return "";
    }
    
}
