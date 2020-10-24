package ir.Expression;

import ir.IrVisitor;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public class IrIntLiteral extends IrLiteral {
    
    public IrIntLiteral(String value) {
        super(value);
        this.setExpType(BaseTypeDescriptor.INT);
    }
    
    public int eval() {
        return 0;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
