package cfg;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nicola
 */

public class CFG {

    private CfgNode start;
    private CfgNode end;
    private Set<CfgNode> nodes = new HashSet<CfgNode>();
    
    public static CFG makeSingleNode(CfgNode node) {
        CFG graph = new CFG();
        graph.start = node;
        graph.end = graph.start;
        graph.nodes.add(node);
        return graph;
    }
    
    public Set<CfgNode> getNodes() {
        return this.nodes;
    }
    
    public void concatenate(CFG block) {
        this.end = block.end;
        block.start = this.end;
        this.addNodes(block);
    }
    
    public void concatenate(CfgNode node) {
        this.end = node;
        nodes.add(node);
    }
    
    private void addNodes(CFG block) {
        for (CfgNode node : block.getNodes()) {
            this.nodes.add(node);
        }
    }
    
}
