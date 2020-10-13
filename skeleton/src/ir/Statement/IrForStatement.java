package ir.Statement;

import ir.Ir;
import ir.Expression.*;

/**
 * @author Nicola
 */
public class IrForStatement extends IrStatement {
    private final IrIdentifier loopVar;
    private final IrExpression startExpr;
    private final IrExpression endExpr;
    private final IrBlock loopBlock;
    
    public IrForStatement(IrIdentifier loopVar, IrExpression startExpr, IrExpression endExpr, IrBlock loopBlock) {
        this.loopVar = loopVar;
        this.startExpr = startExpr;
        this.endExpr = endExpr;
        this.loopBlock = loopBlock;
    }
    
    @Override
    public String toString() {
        String str;
        str = "FOR (\n" + Ir.indent(loopVar.toString()) + "\n" + Ir.indent("=\n");
        str += Ir.indent(startExpr.toString()) + "\n" + Ir.indent("to\n");
        str += Ir.indent(endExpr.toString()) + ")\n";
        str += Ir.indent(loopBlock.toString());
        return str;
    }
    
}
