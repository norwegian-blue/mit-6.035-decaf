package ir.Expression;

public abstract class IrLiteral extends IrExpression {
    
    protected String value;
    
    public IrLiteral(String value) {
        this.value = value;
    }
       
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }
    
}