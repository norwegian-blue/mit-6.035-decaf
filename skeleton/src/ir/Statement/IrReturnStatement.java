package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.IrExpression;

/**
 * @author Nicola
 */
public class IrReturnStatement extends IrStatement {    
    private final IrExpression returnExpr;
    private final boolean returnsVal;
    
    public IrReturnStatement(IrExpression returnExpr) {
        this.returnExpr = returnExpr;
        this.returnsVal = true;
    }
    
    public IrReturnStatement() {
        this.returnExpr = null;
        this.returnsVal = false;
    }
    
    public IrExpression getReturnExp() {
        return this.returnExpr;
    }
    
    public boolean returnsValue() {
        return this.returnsVal;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        String str;
        str = "RETURN";
        if (returnsVal) {
            str += "\n" + Ir.indent(returnExpr.toString());
        }
        return str;
    }
    
    private String inLineStr() {
        String str;
        str = "RETURN";
        if (returnsVal) {
            str += " " + returnExpr;
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
