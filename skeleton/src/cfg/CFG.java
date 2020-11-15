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
    
    public static CFG makeNoOp() {
        CFG graph = new CFG();
        graph.start = new CfgNoOp();
        graph.end = graph.start;
        graph.nodes.add(graph.start);
        return graph;
    }
    
    public Set<CfgNode> getNodes() {
        return this.nodes;
    }
    
    public void addBranches(CFG trueBranch, CFG falseBranch) {
        CfgNode noOp = new CfgNoOp();
        this.end.setTrueBranch(trueBranch.start);
        this.end.setFalseBranch(falseBranch.start);
        CfgNode.concatenate(trueBranch.end, noOp);
        CfgNode.concatenate(falseBranch.end, noOp);
        this.end = noOp;
        this.addNode(noOp);
        this.addNodes(trueBranch);
        this.addNodes(falseBranch);
    }
    
    public void concatenate(CFG block) {
        this.end = block.end;
        block.start = this.end;
        CfgNode.concatenate(this.end, block.start);
        this.addNodes(block);
    }
    
    public void concatenate(CfgNode node) {
        this.end = node;
        CfgNode.concatenate(this.end, node);
        addNode(node);
    }
    
    public void addNodes(CFG block) {
        for (CfgNode node : block.getNodes()) {
            addNode(node);
        }
    }
    
    private void addNode(CfgNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }
    
    public void removeNoOps() {
    }
}
