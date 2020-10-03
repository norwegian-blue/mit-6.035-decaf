package ir.Expression;

/** 
 * @author Nicola
 */
public class IrCalloutExpression extends IrCallExpression {
    private final String name;
    private final IrExpression[] arguments;
    
    public IrCalloutExpression(String name, IrExpression[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}
