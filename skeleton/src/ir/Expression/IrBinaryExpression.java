package ir.Expression;

import ir.Ir;
import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrBinaryExpression extends IrExpression {
    private final IrExpression leftHandSide;
    private final IrExpression rightHandSide;
    private final BinaryOperator operator;
    
    public static enum BinaryOperator {
        PLUS,
        MINUS,
        TIMES,
        DIVIDE,
        MOD,
        LT,
        LE,
        GT,
        GE,
        EQ,
        NEQ,
        OR,
        AND
    }
    
    public IrBinaryExpression(BinaryOperator operator, IrExpression leftHandSide, IrExpression rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
        this.operator = operator;
    }
    
    public IrExpression getLHS() {
        return this.leftHandSide;
    }
    
    public IrExpression getRHS() {
        return this.rightHandSide;
    }
    
    public BinaryOperator getOp() {
        return this.operator;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        return operator.name() + "\n" + Ir.indent(leftHandSide.toString()) 
                               + "\n" + Ir.indent(rightHandSide.toString());
    }
    
    private String inLineStr() {
        return "(" + leftHandSide.toString() + " " + operator.name() + " " + rightHandSide.toString() + ")";
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public boolean isAndExp() {
        return (this.operator == BinaryOperator.AND);
    }
    
    @Override
    public boolean isOrExp() {
        return (this.operator == BinaryOperator.OR);
    }
}
