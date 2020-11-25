package codegen.instructions;

/**
 * @author Nicola
*/

public abstract class LIR {

    public abstract String toCode();
    
    public boolean isLabel() {
        return false;
    }

}
