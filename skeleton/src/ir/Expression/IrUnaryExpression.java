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
    
    @Override
    public String toString() {
        return op.name() + "\n" + Ir.indent(expr.toString());
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
