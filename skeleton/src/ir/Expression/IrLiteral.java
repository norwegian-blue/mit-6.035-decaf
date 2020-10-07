package ir.Expression;

public abstract class IrLiteral extends IrExpression {
    
    private String value;
    
    public IrLiteral(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }

}