package codegen;

/**
 * @author Nicola
 */

public class Variable extends Exp {
    
    private final String varName;
    
    public Variable(String varName) {
        this.varName = varName;
    }
    
    @Override
    public String toString() {
        return varName;
    }
    
    @Override 
    public String toCode() {
        throw new RuntimeException("Variable must be resolved to stack offset");
    }

}
