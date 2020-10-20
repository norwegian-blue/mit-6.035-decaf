package ir.Expression;

import java.util.List;

import ir.Ir;
import ir.IrVisitor;

/** 
 * @author Nicola
 */
public class IrCalloutExpression extends IrCallExpression {
    private final String calloutName;
    private final List<IrExpression> exprArgs;
    private final List<String> strArgs;
    
    public IrCalloutExpression(String calloutName, List<IrExpression> exprArgs, List<String> strArgs) {
        this.calloutName = calloutName;
        this.exprArgs = exprArgs;
        this.strArgs = strArgs;
    }
    
    @Override
    public String toString() {
        String str = "Callout " + calloutName;
        String args = "";
      
        for (IrExpression expr : exprArgs) {
            args += expr.toString() + "\n";
        }
        for (String expr : strArgs) {
            args += expr + "\n";
        }
        
        if (!exprArgs.isEmpty() || !strArgs.isEmpty()) {
            str += " (\n" + Ir.indent(args) + ")";
        }
        
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
