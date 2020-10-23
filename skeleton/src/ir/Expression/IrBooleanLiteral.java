package ir.Expression;

import ir.IrVisitor;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public class IrBooleanLiteral extends IrLiteral {
    
    public IrBooleanLiteral(String value) {
        super(value);
        this.setExpType(BaseTypeDescriptor.BOOL);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
