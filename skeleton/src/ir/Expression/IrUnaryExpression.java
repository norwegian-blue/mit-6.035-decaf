package ir.Expression;

import ir.Ir;
import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrUnaryExpression extends IrExpression {
    private final IrExpression expr;
    private final UnaryOperator op;
    
    public enum UnaryOperator {
        NOT,
        MINUS
    }
    
    public IrUnaryExpression(UnaryOperator op, IrExpression expr) {
        this.expr = expr;
        this.op = op;
    }
    
    public IrExpression getExp() {
        return this.expr;
    }
    
    public UnaryOperator getOp() {
        return this.op;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        return op.name() + "\n" + Ir.indent(expr.toString());
    }
    
    private String inLineStr() {
        return op.name() + expr.toString();
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public boolean isNotExp() {
        return (this.op == UnaryOperator.NOT);
    }
    
    @Override
    public boolean isUnaryMinus() {
        return (this.op == UnaryOperator.MINUS);
    }
    
    @Override
    public expKind getExpKind() {
        return IrExpression.expKind.UN;
    }
    
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof IrUnaryExpression)) {
            return false;
        }
        IrUnaryExpression thatExp = (IrUnaryExpression)that;
        boolean check = (this.op == thatExp.op) && 
                        (this.expr.equals(thatExp.expr));
        return check;
    }
    
    @Override
    public int hashCode() {
        return this.op.hashCode() + this.expr.hashCode();
    }
    
    @Override
    public boolean contains(IrIdentifier exp) {
        return this.expr.equals(exp);
    }
}
