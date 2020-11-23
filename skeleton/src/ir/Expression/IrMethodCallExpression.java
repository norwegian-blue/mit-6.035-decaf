package ir.Expression;

import java.util.List;

import ir.Ir;
import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrMethodCallExpression extends IrCallExpression{
    
    public IrMethodCallExpression(String methodName, List<IrExpression> arguments) {
        super(methodName, arguments);
    }
    
    @Override
    public String toString() {
        String args = "";
        for (IrExpression arg : this.getArgs()) {
            args += arg.toString() + "\n";
        }
        return "function " + this.getName() + "(\n" + Ir.indent(args) + ")";
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
