package cfg;

import java.util.LinkedList;
import java.util.List;

import cfg.Nodes.*;
import codegen.*;
import ir.Declaration.IrVariableDeclaration;
import semantic.*;

/**
 * @author Nicola
 */

public class MethodCFG extends CFG {

    private MethodDescriptor methodDesc;
    
    public MethodCFG(Node root, MethodDescriptor methodDesc) {
        super(root);
        this.methodDesc = methodDesc;
    }
    
    public void addLocal(IrVariableDeclaration local) {
        this.methodDesc.addLocal(new LocalDescriptor(local.getId(), local.getType()));
    }
    
    public MethodCFG blockify() {
        CfgBlock.resetCounter();
        MethodCFG methodBlock = new MethodCFG(this.blockifyTree(this.root), this.methodDesc);
        return methodBlock;
    }
    
    public void assemble(AssemblyProgram prog, SymbolTable table) {

        try {
            table.put(this.methodDesc.getId(), this.methodDesc);
            table.beginScope();
            for (ParameterDescriptor par : this.methodDesc.getPars()) {
                table.put(par.getId(), par);
            }
            for (LocalDescriptor local : this.methodDesc.getLocals()) {
                table.put(local.getId(), local);
            }
        } catch (DuplicateKeyException e) {
            throw new Error("Unexpected error");
        }
               
        CodeGenerator codegen = new CodeGenerator(prog, table, methodDesc.getId());
        this.root.accept(codegen);
        table.endScope();
    }
    
    protected CfgBlock blockifyTree(Node node) {
        
        // Check if already blockified
        if (node.hasParentBlock()) {
            return (CfgBlock)node.getParentBlock();
        }
        
        // Blockify tree
        CfgBlock block = new CfgBlock(node, this.methodDesc.getId());
               
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
        List<Node> nodes = new LinkedList<Node>(this.getNodes());
        for (Node node : nodes) {
            node.accept(flattener);
        }
        this.removeNoOps();
    }
    
}
