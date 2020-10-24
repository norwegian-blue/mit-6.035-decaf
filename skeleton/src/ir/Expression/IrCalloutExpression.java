package ir.Expression;

import java.util.List;

import ir.Ir;
import ir.IrVisitor;

/** 
 * @author Nicola
 */
public class IrCalloutExpression extends IrCallExpression {
    private final String calloutName;
    private final List<IrExpression> args;
    
    public IrCalloutExpression(String calloutName, List<IrExpression> args) {
        this.calloutName = calloutName;
        this.args = args;
    }
    
    @Override
    public String toString() {
        String str = "Callout " + calloutName;
        String arguments = "";
      
        for (IrExpression arg : this.args) {
            arguments += arg.toString() + "\n";
        }

        str += " (\n" + Ir.indent(arguments) + ")";
                
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
