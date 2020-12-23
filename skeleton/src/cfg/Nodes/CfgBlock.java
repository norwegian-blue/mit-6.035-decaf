package cfg.Nodes;

import java.util.ArrayList;
import java.util.List;

import ir.Ir;

/**
 * @author Nicola
 */
public class CfgBlock extends Node {

    private String blockName;
    private Node firstNode;
    private Node lastNode;
    private List<Node> blockNodes = new ArrayList<Node>();
    private static int blockNumber = 0;
    
    public CfgBlock(Node root, String blockNameRoot) {
        this.childNodes = new Node[2];
        root.setParentBlock(this);
        this.firstNode = root;
        this.lastNode = root;
        this.blockName = blockNameRoot + (++blockNumber);
        this.blockNodes.add(root);
    }
    
    public void pullIn(Node node) {
        lastNode = node;
        blockNodes.add(node);
        node.setParentBlock(this);
    }
    
    public static void resetCounter() {
        blockNumber = 0;
    }
    
    public Node getFirstNode() {
        return firstNode;
    }
    
    public Node getLastNode() {
        return lastNode;
    }
    
    public List<Node> getBlockNodes() {
        return this.blockNodes;
    }
    
    @Override
    public boolean isFork() {
        return lastNode.isFork();
    }
               
    @Override
    public boolean hasNext() {
        return lastNode.hasNext();
    }
    
    public CfgBlock getNextBlock() {
        if (this.isFork()) {
            throw new Error("not supported for fork blocks");
        } else {
            return (CfgBlock)this.getLastNode().getNextBranch().getParentBlock();
        }
    }
    
    public CfgBlock getTrueBlock() {
        if (!this.isFork()) {
            throw new Error("not supported for line blocks");
        } else {
            return (CfgBlock)this.getLastNode().getTrueBranch().getParentBlock();
        }
    }
    
    public CfgBlock getFalseBlock() {
        if (!this.isFork()) {
            throw new Error("not supported for line blocks");
        } else {
            return (CfgBlock)this.getLastNode().getFalseBranch().getParentBlock();
        }
    }
    
    @Override
    public String toString() {
        String blockStr = "BLOCK " + getBlockName();
        String nodeStr = "";
        // Print nodes
        for (Node node : blockNodes) {
            nodeStr += "\n" + node.toString();
        }        
        blockStr += Ir.indent(nodeStr, 2) + "\n";
        
        // Print next block
        if (this.isFork()) {
            blockStr += "\t\tCOND T --> " + this.getTrueBlock().getBlockName() + "\n" + 
                        "\t\tCOND F --> " + this.getFalseBlock().getBlockName() + "\n";
        } else if (this.hasNext()) {
            blockStr += "\t\tGOTO   --> " + this.getNextBlock().getBlockName() + "\n";
        }
        
        return blockStr;
    }
    
    public String getBlockName() {
        return blockName;
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.visit(this);
    }
    
    public void prepend(Node original, Node newPredecessor) {
        original.prepend(newPredecessor);
        
        List<Node> newNodes = new ArrayList<Node>();
        for (Node node : this.blockNodes) {
            if (node.equals(original)) {
                newNodes.add(newPredecessor);
            }
            newNodes.add(node);
        }
        this.blockNodes = newNodes;
        
        if (original == firstNode) {
            firstNode = newPredecessor;
        }
    }
    
    @Override
    public CfgBlock getParentBlock() {
        return this;
    }
    
    public void deleteNode(Node node) {
        node.delete();
        
        // Replace first node if necessary
        if (node.equals(this.getFirstNode())) {
            this.firstNode = node.getNextBranch();
        }
        
        // Replace last node if necessary
        if (node.equals(this.getLastNode())) {
            for(Node parent : node.getParents()) {
                this.lastNode = parent;
                break;
            }
        }
        
    }

}
