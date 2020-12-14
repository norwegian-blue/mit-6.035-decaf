package ir.Expression;

import ir.Ir;
import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrIdentifier extends IrExpression {
    private final String idName;
    private final IrExpression arrayInd;
    
    public IrIdentifier(String varName) {
        this.idName = varName;
        this.arrayInd = null;
    }
    
    public IrIdentifier(String idName, IrExpression arrayInd) {
        this.idName = idName;
        this.arrayInd = arrayInd;
    }
    
    public boolean isArrayElement() {
        return arrayInd != null;
    }
    
    public String getId() {
        return this.idName;
    }
    
    public IrExpression getInd() {
        return this.arrayInd;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        String str = idName;
        if (this.isArrayElement()) {
            str += "[\n" + Ir.indent(arrayInd.toString()) + "\n]";
        }
        return str;
    }
    
    private String inLineStr() {
        String str = idName;
        if (this.isArrayElement()) {
            str += "[" + arrayInd.toString() + "]";
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public boolean isAtom() {
        return !this.isArrayElement();
    }
    
    @Override 
    public boolean equals(Object that) {
        if (!(that instanceof IrIdentifier)) {
            return false;
        }
        IrIdentifier thatId = (IrIdentifier)that;
        
        return this.idName.equals(thatId.idName);
    }
}
