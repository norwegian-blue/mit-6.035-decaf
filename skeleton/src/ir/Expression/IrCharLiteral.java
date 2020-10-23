package ir.Expression;

import ir.IrVisitor;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public class IrCharLiteral extends IrLiteral {
   
    public IrCharLiteral(String value) {
        super(value);
        this.setExpType(BaseTypeDescriptor.STRING);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
