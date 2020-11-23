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
        if (!printAsTree) {
            return inLineStr();
        }
        
        String args = "";
        for (IrExpression arg : this.getArgs()) {
            args += arg.toString() + "\n";
        }
        return "function " + this.getName() + "(\n" + Ir.indent(args) + ")";
    }
    
    private String inLineStr() {
        String args = "";
        for (IrExpression arg : this.getArgs()) {
            args += arg.toString() + ", ";
        }
        if (this.getArgs().size() > 0) {
            args = args.substring(0, args.length()-2);
        }
        return this.getName() + "(" + args + ")";
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
