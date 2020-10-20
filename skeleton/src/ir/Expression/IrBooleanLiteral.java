package ir.Expression;

import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrBooleanLiteral extends IrLiteral {
    
    public IrBooleanLiteral(String value) {
        super(value);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
