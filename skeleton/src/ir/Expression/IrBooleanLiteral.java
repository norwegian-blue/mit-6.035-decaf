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
    
    public boolean eval() {
        return Boolean.parseBoolean(this.value);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public expKind getExpKind() {
        return IrExpression.expKind.BOOL;
    }

}
