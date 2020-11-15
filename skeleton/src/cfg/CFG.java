package cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cfg.Nodes.CfgNoOp;
import cfg.Nodes.CfgNode;

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
        trueBranch.end.concatenate(noOp);
        falseBranch.end.concatenate(noOp);
        this.end = noOp;
        this.addNode(noOp);
        this.addNodes(trueBranch);
        this.addNodes(falseBranch);
    }
    
    public void concatenate(CFG block) {
        this.end.concatenate(block.start);
        this.end = block.end;
        block.start = this.end;
        this.addNodes(block);
    }
    
    public void concatenate(CfgNode node) {
        this.end.concatenate(node);
        this.end = node;
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
        for (Iterator<CfgNode> iterator = this.nodes.iterator(); iterator.hasNext();) {
            CfgNode node = iterator.next();
            
            if (node.isNoOp()) {
                // Update parents
                for (CfgNode parent : node.getParents()) {
                    parent.setTrueBranch(node.getTrueBranch());
                    parent.setFalseBranch(node.getFalseBranch());
                }
                
                // Update children
                node.getTrueBranch().setParents(node.getParents());
                node.getFalseBranch().setParents(node.getParents());
                
                // Remove node
                iterator.remove();
            }
        }
    }
}
