package cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cfg.Nodes.*;
import ir.Declaration.IrVariableDeclaration;

/**
 * @author Nicola
 */

public class MethodCFG {

    private Node root;
    private Set<Node> nodes = new HashSet<Node>();
    private List<IrVariableDeclaration> locals = new ArrayList<IrVariableDeclaration>();
    
    public MethodCFG (Node root) {
        this.root = root;
        addNode(root);
        importNodes(root);
    }
    
    public void addLocal(IrVariableDeclaration local) {
        this.locals.add(local);
    }
    
    public Node getRoot() {
        return root;
    }
    
    public void addTrueEdge(Node src, Node dest) {
        assert(checkSource(src));
        addNode(dest);
        src.setTrueBranch(dest);
    }
    
    public void addFalseEdge(Node src, Node dest) {
        assert(checkSource(src));
        addNode(dest);
        src.setFalseBranch(dest);
    }
    
    public void addNextEdge(Node src, Node dest) {
        assert(checkSource(src));
        addNode(dest);
        src.setNextBranch(dest);
    }
    
    private boolean checkSource(Node node) {
        return nodes.contains(node);
    }
    
    private void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }
    
    private void importNodes(Node node) {
        for (Node child : node.getChildren()) {
            if (child == null) continue;
            if (!nodes.contains(child)) {
                addNode(child);
                importNodes(child);
            }
        }
    }

    public void removeNoOps() {
        for (Iterator<Node> iterator = this.nodes.iterator(); iterator.hasNext();) {
            Node node = iterator.next();
            
            if (node.isNoOp()) {
                
                // Update parents
                for (Node parent : node.getParents()) {
                    if (parent.isFork()) {
                        if (parent.isTrueBranch(node)) {
                            parent.setTrueBranch(node.getNextBranch());
                        } else {
                            parent.setFalseBranch(node.getNextBranch());
                        }
                    } else {
                        parent.setNextBranch(node.getNextBranch());
                    }
                }
            
                // Update child
                node.getNextBranch().removeParentNode(node);
                
                // Remove node
                iterator.remove();
            }
        }
    }
}
