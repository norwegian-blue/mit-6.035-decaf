package codegen.Instructions;

/**
 * @author Nicola
 */

public class Jump extends LIR {
    
    private final String destLabel;
    private String cond;
    
    public Jump(String destLabel, String cond) {
        this.destLabel = destLabel;
        this.cond = cond;
    }
        
    @Override
    public String toCode() {
        switch (cond) {
        case "none":
            return "\tjmp\t" + destLabel;
        case "eq":
            return "\tje\t" + destLabel;
        case "lt":
            return "\tjl\t" + destLabel;
        case "ge":
            return "\tjge\t" + destLabel;
        default:
            throw new Error("Unsupported condition");
        }
    }
    
}
