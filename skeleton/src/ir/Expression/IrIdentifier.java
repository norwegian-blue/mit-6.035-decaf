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
    
    public boolean isArray() {
        return arrayInd != null;
    }
    
    @Override
    public String toString() {
        String str = idName;
        if (this.isArray()) {
            str += "[\n" + Ir.indent(arrayInd.toString()) + "\n]";
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
