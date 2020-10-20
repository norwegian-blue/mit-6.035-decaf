package ir.Expression;

import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrCharLiteral extends IrLiteral {
   
    public IrCharLiteral(String value) {
        super(value);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
