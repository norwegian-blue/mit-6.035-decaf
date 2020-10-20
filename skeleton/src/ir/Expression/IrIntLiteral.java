package ir.Expression;

import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrIntLiteral extends IrLiteral {
    
    public IrIntLiteral(String value) {
        super(value);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
