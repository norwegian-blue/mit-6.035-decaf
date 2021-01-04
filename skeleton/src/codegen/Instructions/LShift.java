package codegen.Instructions;

/**
 * @author Nicola
 */
public class LShift extends LIR {
    
    private Exp reg;
    
    public LShift(Exp reg) {
        this.reg = reg;
    }

    @Override
    public String toCode() {
        return "\tshl" + reg.getSuffix() + "\t" + reg.toCode(); 
    }

}
    