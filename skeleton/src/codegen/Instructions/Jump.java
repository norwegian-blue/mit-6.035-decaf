package codegen.Instructions;

/**
 * @author Nicola
 */

public class Jump extends LIR {
    
    // TODO recheck
    private final Label destLabel;
    
    public Jump(Label destLabel) {
        this.destLabel = destLabel;
    }
    
    @Override
    public String toString() {
        return "JMP -> " + destLabel.toString();
    }
    
    @Override
    public String toCode() {
        return "jmp " + destLabel.toCode();
    }
    
}
