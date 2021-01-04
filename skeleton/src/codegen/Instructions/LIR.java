package codegen.Instructions;

/**
 * @author Nicola
*/

public abstract class LIR {

    public abstract String toCode();
    
    public boolean isLabel() {
        return false;
    }
    
    public boolean isString() {
        return false;
    }

    public boolean isErrorHandler() {
        return false;
    }
    
    @Override 
    public String toString() {
        return this.toCode();
    }

}
