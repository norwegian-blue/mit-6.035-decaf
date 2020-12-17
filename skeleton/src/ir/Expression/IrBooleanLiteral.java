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
    
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof IrBooleanLiteral)) {
            return false;
        }
        IrBooleanLiteral thatBool = (IrBooleanLiteral)that;
        return this.value.equals(thatBool.value);
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

}
