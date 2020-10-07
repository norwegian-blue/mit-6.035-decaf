package ir.Expression;

import java.util.List;

import ir.Ir;

/**
 * @author Nicola
 */
public class IrMethodCallExpression extends IrCallExpression{
    private final String methodName;
    private final List<IrExpression> arguments;
    
    public IrMethodCallExpression(String methodName, List<IrExpression> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }
    
    @Override
    public String toString() {
        String args = "";
        for (IrExpression arg : arguments) {
            args += arg.toString() + "\n";
        }
        return "function " + methodName + "(\n" + Ir.indent(args) + ")";
    }
}
