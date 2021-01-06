package codegen.Instructions;

import java.util.ArrayList;
import java.util.List;

public class Call extends LIR {

    private String method;
    private static int base;
    private static int ind;
    
    public Call(String method) {
        this.method = method;
    }

    @Override
    public String toCode() {
        return "\tcall\t" + method;
    }
    
    public static void resetBase() {
        base = 16;
        ind = 1;
    }
    
    public static Location getParam(int size) {
        switch (ind++) {
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
            int inc = (size == 1) ? 2 : size;
            base += inc;
            return new Memory(base-inc, size);
        }
    }
    
    public static Location getParamAtIndex(int i, int size) {
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
            throw new Error("unknown memory location");
        }
    }
    
    public static List<Register> getCalleeSaved() {
        List<Register> calleeSaved = new ArrayList<Register>();
        calleeSaved.add(Register.rax());
        calleeSaved.add(Register.rcx());
        calleeSaved.add(Register.rdx());
        calleeSaved.add(Register.rsi());
        calleeSaved.add(Register.rdi());
        calleeSaved.add(Register.r8());
        calleeSaved.add(Register.r9());
        calleeSaved.add(Register.r11());
        
        return calleeSaved;
    }
    
}
