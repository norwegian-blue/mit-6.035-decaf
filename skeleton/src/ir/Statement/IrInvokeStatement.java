package ir.Statement;

import ir.Ir;
import ir.Expression.IrCallExpression;

/**
 * @author Nicola
 */
public class IrInvokeStatement extends IrStatement {
    
    private final IrCallExpression methodCall;
    
    public IrInvokeStatement(IrCallExpression methodCall) {
        this.methodCall = methodCall;
    }
    
    @Override
    public String toString() {
        return "CALL:\n" + Ir.indent(methodCall.toString());
    }
}
