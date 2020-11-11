package ir.Expression;

import ir.IrVisitor;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public class IrStringLiteral extends IrLiteral {
   
    public IrStringLiteral(String value) {
        super(value);
        this.setExpType(BaseTypeDescriptor.STRING);
    }
    
    public String eval() {
        return this.value;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public String toString() {
        return this.value.replaceAll("\\%", "\\%\\%");
    }
}
