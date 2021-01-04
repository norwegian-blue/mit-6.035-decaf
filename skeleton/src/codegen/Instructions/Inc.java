package codegen.Instructions;

/**
 * @author Nicola
 */
public class Inc extends LIR {
    
    private Exp loc;
    
    public Inc(Exp loc) {
        this.loc = loc;
    }

    @Override
    public String toCode() {
        return "\tincq\t" + loc.toCode(); 
    }

}
