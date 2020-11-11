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
}