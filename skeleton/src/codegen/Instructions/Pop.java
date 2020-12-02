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
        return "\tpop\t" + destReg.toCode();
    }
    
    
}
