package codegen.Instructions;

public class Call extends LIR {

    private String method;
    
    public Call(String method) {
        this.method = method;
    }

    @Override
    public String toCode() {
        return "\tcall\t" + method;
    }
    
    public static Exp getParamAtIndex(int i) {
        switch (i) {
        case 1:
            return new Register(Register.Registers.rdi);
        case 2:
            return new Register(Register.Registers.rsi);
        case 3:
            return new Register(Register.Registers.rdx);
        case 4:
            return new Register(Register.Registers.rcx);
        case 5:
            return new Register(Register.Registers.r8);
        case 6:
            return new Register(Register.Registers.r9);
        default:
            return new Local((i-6)*8);
        }
    }
    
}
