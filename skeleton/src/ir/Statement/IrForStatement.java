package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.*;

/**
 * @author Nicola
 */
public class IrForStatement extends IrStatement {
    private final IrIdentifier loopVar;
    private IrExpression startExpr;
    private IrExpression endExpr;
    private final IrBlock loopBlock;
    
    public IrForStatement(IrIdentifier loopVar, IrExpression startExpr, IrExpression endExpr, IrBlock loopBlock) {
        this.loopVar = loopVar;
        this.startExpr = startExpr;
        this.endExpr = endExpr;
        this.loopBlock = loopBlock;
    }
    
    public IrIdentifier getLoopVar() {
        return this.loopVar;
    }
    
    public IrExpression getStartExp() {
        return this.startExpr;
    }
    
    public void setStartExp(IrExpression startExpr) {
        this.startExpr = startExpr;
    }
    
    public IrExpression getEndExp() {
        return this.endExpr;
    }
    
    public void setEndExp(IrExpression endExp) {
        this.endExpr = endExp;
    }
    
    public IrBlock getLoopBlock() {
        return this.loopBlock;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        String str;
        str = "FOR (\n" + Ir.indent(loopVar.toString()) + "\n" + Ir.indent("=\n");
        str += Ir.indent(startExpr.toString()) + "\n" + Ir.indent("to\n");
        str += Ir.indent(endExpr.toString()) + ")\n";
        str += Ir.indent(loopBlock.toString());
        return str;
    }
    
    private String inLineStr() {
        String str;
        str = "FOR (" + loopVar.toString() + " = ";
        str += startExpr.toString() + " to ";
        str += endExpr.toString() + ") {";
        String loopStr = loopBlock.toString();
        str += Ir.indent(loopStr.substring(1, loopStr.length()-1));
        return str + "}";
    }        
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
}
