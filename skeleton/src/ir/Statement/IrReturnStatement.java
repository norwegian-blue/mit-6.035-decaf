package ir.Statement;

import ir.Ir;
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
    
    @Override
    public String toString() {
        String str;
        str = "RETURN";
        if (!returnVoid) {
            str += "\n" + Ir.indent(returnExpr.toString());
        }
        return str;
    }
}
