package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.IrExpression;

public class IrIfStatement extends IrStatement {
    private final IrExpression condition;
    private final IrBlock thenBlock;
    private final IrBlock elseBlock;
    
    public IrIfStatement(IrExpression condition, IrBlock thenBlock, IrBlock elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
    
    public IrIfStatement(IrExpression condition, IrBlock thenBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = new IrBlock();
    }
    
    @Override
    public String toString() {
        String str;
        str = "IF (\n" + Ir.indent(condition.toString()) + "\n)\n";
        str += "THEN\n" + Ir.indent(thenBlock.toString());
        if (!elseBlock.isEmpty()) {
            str += "\nELSE\n" + Ir.indent(elseBlock.toString());
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
}
