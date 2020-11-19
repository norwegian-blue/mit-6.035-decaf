package cfg.Nodes;

public class CfgNoOp extends CfgLineNode {
    
    public CfgNoOp() {
        super();
    }
    
    @Override
    public boolean isNoOp() {
        return true;
    }
    
    @Override
    public String nodeString() {
        return "NODE NOOP";
    }
    
}
