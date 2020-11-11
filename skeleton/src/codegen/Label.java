package codegen;

/**
 * @author Nicola
 */

public class Label extends LIR {

    private final String labelName;
    
    public Label(String labelName) {
        this.labelName = labelName;
    }
    
    @Override
    public String toString() {
        return "LABEL: " + this.labelName;
    }
    
    @Override
    public String toCode() {
        return "." + this.labelName;
    }
    
}
