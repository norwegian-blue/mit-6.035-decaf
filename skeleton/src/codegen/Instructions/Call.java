package codegen.Instructions;

import java.util.ArrayList;
import java.util.List;

import semantic.ParameterDescriptor;

public class Call extends LIR {

    private String method;
    
    public Call(String method) {
        this.method = method;
    }

    @Override
    public String toCode() {
        return "\tcall\t" + method;
    }
        
    public static Location getParam(List<ParameterDescriptor> pars, int ind) {
        switch (ind) {
        case 0:
            return Register.rdi();
        case 1:
            return Register.rsi();
        case 2:
            return Register.rdx();
        case 3:
            return Register.rcx();
        case 4:
            return Register.r8();
        case 5:
            return Register.r9();
        default:
            int offset = 16;
            int size = pars.get(ind).getSize();
            int memSize = (size == 1) ? 2 : 8;
            for (int i = 6; i <= ind; i++) {
                offset += (pars.get(i).getSize() == 1) ? 2 : 8;
            }
            return new Memory(offset-memSize, size);
        }
    }
    
    public static Location getParamAtIndex(int i) {
        switch (i) {
        case 0:
            return Register.rdi();
        case 1:
            return Register.rsi();
        case 2:
            return Register.rdx();
        case 3:
            return Register.rcx();
        case 4:
            return Register.r8();
        case 5:
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
