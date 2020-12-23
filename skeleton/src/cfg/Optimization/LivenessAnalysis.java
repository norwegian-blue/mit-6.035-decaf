package cfg.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cfg.MethodCFG;
import cfg.Nodes.CfgBlock;
import cfg.Nodes.CfgCondBranch;
import cfg.Nodes.CfgEntryNode;
import cfg.Nodes.CfgExitNode;
import cfg.Nodes.CfgStatement;
import cfg.Nodes.Node;
import cfg.Nodes.NodeVisitor;
import ir.Expression.IrExpression;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;
import ir.Statement.IrInvokeStatement;
import ir.Statement.IrStatement;

public class LivenessAnalysis implements NodeVisitor<Void> {
    
    private Map<CfgBlock, LiveVariables> LVin;
    private Map<CfgBlock, LiveVariables> LVout;
    
    public LivenessAnalysis() {
        this.LVin = new HashMap<CfgBlock, LiveVariables>();;
        this.LVout = new HashMap<CfgBlock, LiveVariables>();
    }
    
    public void analyze(MethodCFG cfg) {
        
        // Initialize list of blocks
        Set<Node> changed = new HashSet<Node>(cfg.getNodes());
        for (Node block : changed) {
            LVout.put(block.getParentBlock(), new LiveVariables());
            LVin.put(block.getParentBlock(), new LiveVariables());
        }
                
        // Iteratively solve dataflow equations
        while(!changed.isEmpty()) {  
            
            Node currentNode = changed.iterator().next();   
            changed.remove(currentNode);
            
            // Live out = intersect children
            LiveVariables LVout_n = new LiveVariables();
            for (Node children : currentNode.getChildren()) {
                if (children == null) {
                    continue;
                }
                LVout_n.intersect(LVin.get(children));
            }
            LVout.put(currentNode.getParentBlock(), LVout_n);
                        
            // Available out = Copied union (In-Killed)
            LiveVariables LVin_old = new LiveVariables(LVin.get(currentNode.getParentBlock()));
            currentNode.accept(this);
            
            // Re-iterate if changed         
            if (!LVin_old.equals(LVin.get(currentNode.getParentBlock()))) {
                for (Node parent : currentNode.getParents()) {
                    changed.add(parent);
                }
            }
        }
    }

    @Override
    public Void visit(CfgBlock node) {
        // Initialize LVin from LVout and update LVin from block end to start
        LVin.put(node.getParentBlock(), new LiveVariables(LVout.get(node.getParentBlock())));
        for (int i = node.getBlockNodes().size(); i > 0; i--) {
            node.getBlockNodes().get(i-1).accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CfgCondBranch node) {
        node.setLiveVar(LVin.get(node.getParentBlock()).getLiveVars());     // inherit from successor
        node.setLiveVar(node.getExp().getUsedVars());                       // update node with used vars
        LVin.get(node.getParentBlock()).addLiveVar(node.getLiveVars());     // update LVin map
        return null;
    }

    @Override
    public Void visit(CfgEntryNode node) {
        node.setLiveVar(LVin.get(node.getParentBlock()).getLiveVars());     // inherit from successor
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        if (node.returnsExp()) {
            node.setLiveVar(node.getExp().getUsedVars());                   // set returned variables to live
            LVin.get(node.getParentBlock()).addLiveVar(node.getLiveVars()); // update LVin map
        }
        return null;
    }

    @Override
    public Void visit(CfgStatement node) {
        
        LiveVariables lv = LVin.get(node.getParentBlock());
        
        // Function call
        IrStatement stm = node.getStatement();                              
        
        if (!stm.isAssignment()) {                             // Add used variables and updated node info
            IrInvokeStatement call = (IrInvokeStatement) stm;
            lv.addLiveVar(call.getMethod().getUsedVars());
            node.setLiveVar(lv.getLiveVars());
            return null;
        }
        
        // Assignement
        IrAssignment ass = (IrAssignment) stm;
        IrIdentifier id = ass.getLocation();
        IrExpression exp = ass.getExpression();
        
        lv.addLiveVar(exp.getUsedVars());                       // Add used variables
        node.setLiveVar(lv.getLiveVars());                      // Update node info
        if (!exp.getUsedVars().contains(id)) {
            lv.removeLiveVar(id);                               // Removed assigned variable
        }
        
        return null;
    }
    
    private class LiveVariables {
        
        private Set<IrIdentifier> liveVars;
        
        public LiveVariables() {
            this.liveVars = new HashSet<IrIdentifier>();
        }
        
        public LiveVariables(LiveVariables lv) {
            this.liveVars = new HashSet<IrIdentifier>(lv.liveVars);
        }
        
        public Set<IrIdentifier> getLiveVars() {
            return this.liveVars;
        }
        
        public void addLiveVar(Set<IrIdentifier> ids) {
            liveVars.addAll(ids);
        }
        
        public void removeLiveVar(IrIdentifier id) {
            liveVars.remove(id);
        }
        
        public void intersect(LiveVariables lv) {
            this.addLiveVar(lv.getLiveVars());
        }
        
        @Override
        public int hashCode() {
            return liveVars.size();
        }
        
        @Override
        public boolean equals(Object that) {
            if (!(that instanceof LiveVariables)) {
                return false;
            }
            LiveVariables thatLV = (LiveVariables) that;
            for (IrIdentifier id : thatLV.liveVars) {
                if (!this.liveVars.contains(id)) {
                    return false;
                }
            }
            return thatLV.liveVars.size() == this.liveVars.size();
        }
        
        @Override
        public String toString() {
            return this.liveVars.toString();
        }
        
    }

}
