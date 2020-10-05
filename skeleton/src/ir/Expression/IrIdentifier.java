package ir.Expression;

/**
 * @author Nicola
 */
public class IrIdentifier extends IrExpression {
    private final String varName;
    
    public IrIdentifier(String varName) {
        this.varName = varName;
    }
    
    @Override
    public String toString() {
        return this.varName;
    }
}
