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
        return this.value.substring(1, this.value.length()-1);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public String toString() {
        return this.value.replaceAll("\\%", "\\%\\%");
    }
    
    @Override
    public expKind getExpKind() {
        return IrExpression.expKind.STRING;
    }
    
}
