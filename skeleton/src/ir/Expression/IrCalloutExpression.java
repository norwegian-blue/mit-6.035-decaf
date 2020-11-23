package ir.Expression;

import java.util.List;

import ir.Ir;
import ir.IrVisitor;

/** 
 * @author Nicola
 */
public class IrCalloutExpression extends IrCallExpression {
    
    public IrCalloutExpression(String calloutName, List<IrExpression> arguments) {
        super(calloutName, arguments);
    }
    
    @Override
    public String toString() {
        
        if(!printAsTree) {
            return inLineStr();
        }
        
        String str = "Callout " + this.getName();
        String arguments = "";
      
        for (IrExpression arg : this.getArgs()) {
            arguments += arg.toString() + "\n";
        }

        str += " (\n" + Ir.indent(arguments) + ")";
                
        return str;
    }
    
    private String inLineStr() {
        String str = "Callout(" + this.getName() + ", ";
        String arguments = "";
      
        if (this.getArgs().size() > 0) {
            for (IrExpression arg : this.getArgs()) {
                arguments += arg.toString() + ", ";
            }
            arguments = arguments.substring(0, arguments.length()-2);
        }

        str += arguments + ")";
                
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
