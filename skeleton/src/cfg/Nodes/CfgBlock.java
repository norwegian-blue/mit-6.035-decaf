package cfg.Nodes;

import java.util.ArrayList;
import java.util.List;

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
    
    @Override
    public boolean isFork() {
        return lastNode.isFork();
    }
               
    @Override
    public boolean hasNext() {
        return lastNode.hasNext();
    }
    
    public Node getNextBlock() {
        if (this.isFork()) {
            throw new Error("not supported for fork blocks");
        } else {
            return this.getLastNode().getNextBranch().getParentBlock();
        }
    }
    
    public Node getTrueBlock() {
        if (!this.isFork()) {
            throw new Error("not supported for line blocks");
        } else {
            return this.getLastNode().getTrueBranch().getParentBlock();
        }
    }
    
    public Node getFalseBlock() {
        if (!this.isFork()) {
            throw new Error("not supported for line blocks");
        } else {
            return this.getLastNode().getFalseBranch().getParentBlock();
        }
    }

    @Override
    public String toString() {
        String blockStr = this.nodeString();
        if (this.isFork()) {
            blockStr += ":" +
                        "\n\tTrueBranch:  " + this.getTrueBlock().nodeString() +
                        "\n\tFalseBranch: " + this.getFalseBlock().nodeString();
        } else if (this.hasNext()) {
            blockStr += " -> " + this.getNextBlock().nodeString();
        } else {
            blockStr += " -> RETURN";
        }
        return blockStr;
    }
    
    @Override
    public String nodeString() {
        return getBlockName();
    }
    
    public String getBlockName() {
        return "BLOCK " + blockName;
    }

}
