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
    
    public static String getSuffix(Exp e1, Exp e2) {
        if (e1.getSuffix().equals("b") || e2.getSuffix().equals("b")) {
            return "b";
        } else {
            return "q";
        }
    }

}
