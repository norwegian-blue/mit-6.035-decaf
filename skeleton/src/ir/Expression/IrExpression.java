package ir.Expression;

import ir.Ir;
import semantic.*;

/**
 * @author Nicola
 */
public abstract class IrExpression extends Ir {
    
    private TypeDescriptor expType = BaseTypeDescriptor.undefined;
    
    public void setExpType(TypeDescriptor type) {
        this.expType = type;
    }
    
    public TypeDescriptor getExpType() {
        return expType;
    }
    
    public boolean isAndExp() {
        return false;
    }
    
    public boolean isOrExp() {
        return false;
    }
    
    public boolean isNotExp() {
        return false;
    }
    
    @Override
    public boolean isExp() {
        return true;
    }
    
    public boolean isAtom() {
        return false;
    }
    
    public boolean isLiteral() {
        return false;
    }
    
}