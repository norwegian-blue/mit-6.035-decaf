package codegen.Instructions;

/**
 * @author Nicola
 */
public class Pop extends LIR {

    private Register destReg;
    private String suffix;
    
    public Pop(Register destReg) {
        this.destReg = destReg;
        this.suffix = destReg.getSuffix();
    }
    
    @Override
    public String toCode() {
        if (suffix.equals("b")) {
            suffix = "w";
            destReg = new Register(destReg, 2);
        }
        return "\tpop" + suffix + "\t" + destReg.toCode();
    }
    
    
}
