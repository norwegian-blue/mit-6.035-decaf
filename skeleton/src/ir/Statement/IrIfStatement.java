package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.IrExpression;

public class IrIfStatement extends IrStatement {
    private IrExpression condition;
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
    
    public IrExpression getCondition() {
        return this.condition;
    }
    
    public void setCondition(IrExpression condition) {
        this.condition = condition;
    }
    
    public IrBlock getThenBlock() {
        return this.thenBlock;
    }
    
    public IrBlock getElseBlock() {
        return this.elseBlock;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        String str;
        str = "IF (\n" + Ir.indent(condition.toString()) + "\n)\n";
        str += "THEN\n" + Ir.indent(thenBlock.toString());
        if (!elseBlock.isEmpty()) {
            str += "\nELSE\n" + Ir.indent(elseBlock.toString());
        }
        return str;
    }
    
    private String inLineStr() {
        String str;
        String thenBlockStr = thenBlock.toString();
        String elseBlockStr = "";
        if (!elseBlock.isEmpty()) {
            elseBlockStr = elseBlock.toString();
        }
        
        str = "IF (" + condition.toString() + ") {";
        str += Ir.indent(thenBlockStr.substring(1, thenBlockStr.length()-1));
        str += "}";
        
        if (!elseBlock.isEmpty()) {
            str += " ELSE {" + Ir.indent(elseBlockStr.substring(1, elseBlockStr.length()-1)) + "}";
        }
        return str;
    }        
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
}
