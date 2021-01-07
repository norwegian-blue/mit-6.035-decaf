package codegen.Instructions;

public abstract class Exp extends LIR {
    
    protected int size;
    
    public boolean isReg() {
        return false;
    }
    
    public boolean isLiteral() {
        return false;
    }
    
    public boolean isImm() {
        return isReg() || isLiteral();
    }
    
    public int getSize() {
        return this.size;
    }
        
    public String getSuffix() {
        switch (this.size) {
        case (1):
            return "b";
        case (8):
            return "q";
        default:
            return "";
        }
    }
    
}
