package cfg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cfg.Nodes.*;
import ir.Declaration.IrVariableDeclaration;

/**
 * @author Nicola
 */

public class MethodCFG extends CFG {

    private List<IrVariableDeclaration> locals = new ArrayList<IrVariableDeclaration>();
    private String name;
    
    public MethodCFG(Node root, String name) {
        super(root);
        this.name = name;
    }
    
    public void addLocal(IrVariableDeclaration local) {
        this.locals.add(local);
    }
    
    public MethodCFG blockify() {
        CfgBlock.resetCounter();
        MethodCFG methodBlock = new MethodCFG(this.blockifyTree(this.root), name);
        methodBlock.locals = this.locals;
        //System.out.println(methodBlock);
        return methodBlock;
    }
    
    protected CfgBlock blockifyTree(Node node) {
        
        // Check if already blockified
        if (node.hasParentBlock()) {
            return (CfgBlock)node.getParentBlock();
        }
        
        // Blockify tree
        CfgBlock block = new CfgBlock(node, name);
               
        while (block.hasNext()) {
            
            // Fork block
            if (block.isFork()) {
                block.setTrueBranch(blockifyTree(block.getLastNode().getTrueBranch()));
                block.setFalseBranch(blockifyTree(block.getLastNode().getFalseBranch()));
                break;
            }
            
            // (so far) Line block
            Node next = block.getLastNode().getNextBranch();            
            if (next.isMerge() || next.hasParentBlock()) {
                block.setNextBranch(blockifyTree(next));
                break;
            }
            block.pullIn(next);
        }
        
        return block;
    }
    
    public void flatten() {
        NodeFlattener flattener = new NodeFlattener(this);  
        for (Node node : this.getNodes()) {
            node.accept(flattener);
        }
        this.removeNoOps();
    }
    
}
