package codegen.Instructions;

/**
 * @author Nicola
 */
public class Pop extends LIR {

    private Register destReg;
    
    public Pop(Register destReg) {
        this.destReg = destReg;
    }
    
    @Override
    public String toCode() {
        String suffix = "q";
        if (destReg.getSuffix().equals("b")) {
            suffix = "w";
        }
        return "\tpop" + suffix + "\t" + destReg.toCode();
    }
    
    
}
