package codegen.Instructions;

public abstract class Exp extends LIR {
    
    protected int size;
    
    public boolean isReg() {
        return false;
    }
    
    public static String getSuffix(int size) {
        switch (size) {
        case (1):
            return "b";
        case (8):
            return "q";
        default:
            throw new Error("Unexpected");
        }
    }
    
    public String getSuffix1() {
        switch (this.size) {
        case (1):
            return "b";
        case (8):
            return "q";
        default:
            throw new Error("Unexpected");
        }
    }
    
    // TODO size as expression property
    
}
