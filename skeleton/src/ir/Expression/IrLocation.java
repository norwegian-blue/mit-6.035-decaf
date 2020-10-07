package ir.Expression;

import ir.Ir;

/**
 * @author Nicola
 */
public class IrLocation extends IrExpression {
    private final String varName;
    private final IrExpression arrayInd;
    
    public IrLocation(String varName) {
        this.varName = varName;
        this.arrayInd = null;
    }
    
    public IrLocation(String varName, IrExpression arrayInd) {
        this.varName = varName;
        this.arrayInd = arrayInd;
    }
    
    public boolean isArray() {
        return arrayInd != null;
    }
    
    @Override
    public String toString() {
        String str = varName;
        if (this.isArray()) {
            str += "[\n" + Ir.indent(arrayInd.toString()) + "\n]";
        }
        return str;
    }
}
