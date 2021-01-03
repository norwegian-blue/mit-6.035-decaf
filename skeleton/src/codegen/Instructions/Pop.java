package codegen.Instructions;

/**
 * @author Nicola
 */
public class Pop extends LIR {

    private Register destReg;
    private int size;
    
    public Pop(Register destReg) {
        this.destReg = destReg;
        this.size = 8;
    }
    
    public Pop(Register destReg, int size) {
        this.destReg = destReg;
        this.size = size;
    }

    @Override
    public String toCode() {
        return "\tpop" + Exp.getSuffix(size) + "\t" + destReg.toCode();
    }
    
    
}
