package cfg.Nodes;

public class CfgNoOp extends CfgNode {
    
    @Override
    public boolean isNoOp() {
        return true;
    }
    
    @Override
    public String toString() {
        return "NOOP";
    }

}
