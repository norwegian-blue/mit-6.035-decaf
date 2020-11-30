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
            return Register.rdi();
        case 2:
            return Register.rsi();
        case 3:
            return Register.rdx();
        case 4:
            return Register.rcx();
        case 5:
            return Register.r8();
        case 6:
            return Register.r9();
        default:
            return new Local(8+(i-6)*8);
        }
    }
    
}
