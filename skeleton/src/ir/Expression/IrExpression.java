package ir.Expression;

import ir.Ir;
import semantic.*;

/**
 * @author Nicola
 */
public abstract class IrExpression extends Ir {
    
    private TypeDescriptor expType = BaseTypeDescriptor.undefined;
    
    public enum expKind {
            BIN,
            BOOL,
            CALL,
            ID,
            INT,
            METH,
            STRING,
            UN
    }
            
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
    
    public boolean isUnaryMinus() {
        return false;
    }
    
    @Override
    public boolean isExp() {
        return true;
    }
    
    public boolean isAtom() {
        return false;
    }
    
    public expKind getExpKind() {
        throw new UnsupportedOperationException();
    }
    
    public boolean isLiteral() {
        return false;
    }
    
    public boolean contains(IrIdentifier var) {
        return false;
    }
    
}