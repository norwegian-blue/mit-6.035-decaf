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
    
    @Override
    public String nodeString() {
        return "NODE EXIT";
    }
    
    @Override
    public boolean hasNext() {
        return false;
    }
    
}
