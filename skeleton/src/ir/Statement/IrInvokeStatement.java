package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.IrCallExpression;

/**
 * @author Nicola
 */
public class IrInvokeStatement extends IrStatement {
    
    private IrCallExpression methodCall;
    
    public IrInvokeStatement(IrCallExpression methodCall) {
        this.methodCall = methodCall;
    }
    
    public IrCallExpression getMethod() {
        return this.methodCall;
    }
    
    public void setMethod(IrCallExpression method) {
        this.methodCall = method;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        return "CALL:\n" + Ir.indent(methodCall.toString());
    }
    
    private String inLineStr() {
        return methodCall.toString();
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public boolean isInvokeStatement() {
        return true;
    }
}
