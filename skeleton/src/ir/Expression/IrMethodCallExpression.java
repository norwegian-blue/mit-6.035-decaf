package ir.Expression;

/**
 * @author Nicola
 */
public class IrMethodCallExpression extends IrCallExpression{
    private final String methodName;
    private final IrExpression[] arguments;
    
    public IrMethodCallExpression(String methodName, IrExpression[] arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }
}
