package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.IrExpression;

/**
 * @author Nicola
 */
public class IrReturnStatement extends IrStatement {    
    private final IrExpression returnExpr;
    private final boolean returnVoid;
    
    public IrReturnStatement(IrExpression returnExpr) {
        this.returnExpr = returnExpr;
        this.returnVoid = false;
    }
    
    public IrReturnStatement() {
        this.returnExpr = null;
        this.returnVoid = true;
    }
    
    public IrExpression getReturnExp() {
        return this.returnExpr;
    }
    
    public boolean returnsVoid() {
        return this.returnVoid;
    }
    
    @Override
    public String toString() {
        String str;
        str = "RETURN";
        if (!returnVoid) {
            str += "\n" + Ir.indent(returnExpr.toString());
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
