package ir.Expression;

/**
 * @author Nicola
 */
public class IrIntLiteral extends IrLiteral {
    private String intValue;
    
    public void IntLiteral(String intValue) {
        this.intValue = intValue;
    }
}
