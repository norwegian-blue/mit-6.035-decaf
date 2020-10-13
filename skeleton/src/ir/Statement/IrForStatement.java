package ir.Statement;

import ir.Ir;
import ir.Expression.*;

/**
 * @author Nicola
 */
public class IrForStatement extends IrStatement {
    private final IrIdentifier loopVar;
    private final IrExpression loopExpr;
    private final IrBlock loopBlock;
    
    public IrForStatement(IrIdentifier loopVar, IrExpression loopExpr, IrBlock loopBlock) {
        this.loopVar = loopVar;
        this.loopExpr = loopExpr;
        this.loopBlock = loopBlock;
    }
    
    @Override
    public String toString() {
        String str;
        str = "FOR (\n" + Ir.indent(loopVar.toString() + "\n:\n" + loopExpr.toString() + "\n)\n");
        str += Ir.indent(loopBlock.toString());
        return str;
    }
    
}
