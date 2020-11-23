package cfg.Nodes;

import ir.Expression.IrExpression;

/**
 * @author Nicola
 */
public class CfgExitNode extends CfgLineNode {
    
    private IrExpression returnExp;
       
    public CfgExitNode() {
        super();
    }
    
    public CfgExitNode(IrExpression returnExp) {
        this.returnExp = returnExp;
    }
    
    public boolean returnsExp() {
        return returnExp != null;
    }
    
    public IrExpression getReturnExp() {
        if (returnsExp()) {
            return this.returnExp;
        } else {
            throw new Error("This method does not return an expression");
        }
    }
    
    public void setReturnExp(IrExpression exp) {
        this.returnExp = exp;
    }
    
    @Override
    public String nodeString() {
        String str = "NODE EXIT";
        if (printIr && this.returnsExp()) {
            str += ": return " + returnExp.toString();
        }
        return str;
    }
    
    @Override
    public boolean hasNext() {
        return false;
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public void setNextBranch(Node node) {
    }
    
}
