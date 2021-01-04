package codegen.Instructions;

/**
 * @author Nicola
 */
public class Dec extends LIR {
    
    private Exp loc;
    
    public Dec(Exp loc) {
        this.loc = loc;
    }

    @Override
    public String toCode() {
        return "\tdecq\t" + loc.toCode(); 
    }

}