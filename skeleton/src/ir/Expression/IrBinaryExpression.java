package ir.Expression;

/**
 * @author Nicola
 */
public class IrBinaryExpression extends IrExpression {
    private final IrExpression leftHandSide;
    private final IrExpression rightHandSide;
    private final String operator;
    
    public IrBinaryExpression(String operator, IrExpression leftHandSide, IrExpression rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
        this.operator = operator;
    }
}
