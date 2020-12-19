package cfg.Optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.*;
import ir.Statement.IrAssignment;
import ir.Statement.IrInvokeStatement;

/**
 * @author Nicola
 */

public class CSE {
    
    private int tmpStart;
    private List<IrIdentifier> newTmps;
    
    private Map<CfgBlock, AvailableExpression> AEin;
    private Map<CfgBlock, AvailableExpression> AEout;
    
    public CSE(int tmpStart) {
        this.tmpStart = tmpStart;
        this.newTmps = new ArrayList<IrIdentifier>();
        this.AEin = new HashMap<CfgBlock, AvailableExpression>();
        this.AEout = new HashMap<CfgBlock, AvailableExpression>();
    }
    
    public List<IrIdentifier> getNewTmps() {
        return this.newTmps;
    }
    
    public boolean optimize(MethodCFG cfg){
        
        boolean change = false;
        
        // Local
        LocalCSE local = new LocalCSE(this.tmpStart);        
        for (Node block : cfg.getNodes()) {
            change |= block.accept(local);
            this.newTmps.addAll(local.getNewTmps());
        }
        
        // Get available expressions
        getAvailableExpressions(cfg);
        
        // Global 
        // TODO implement global CSE
        
        return change;
    }    
    
    
    //////////////////////////////////////////////////////
    //      AVAILABLE EXPRESSIONS DATAFLOW ANALYSIS     //
    //////////////////////////////////////////////////////
    
    private void getAvailableExpressions(MethodCFG cfg) {
    
        // GEN and DEF
        GenExpressionFinder GEN = new GenExpressionFinder();
        DefinitionFinder DEF = new DefinitionFinder();
        
        // Initialize available outlet/inlet expressions to empty set for all blocks
        for (Node block : cfg.getNodes()) {
            AEin.put(block.getParentBlock(), new AvailableExpression());
            AEout.put(block.getParentBlock(), new AvailableExpression());
        }
        
        // Initialize list of blocks
        Set<Node> changed = new HashSet<Node>(cfg.getNodes());
        changed.remove(cfg.getRoot());
        
        // Process entry block
        cfg.getRoot().accept(GEN);
        AvailableExpression AEgen = new AvailableExpression(cfg.getRoot().getParentBlock(), GEN.getGenExpression());
        AEout.put(cfg.getRoot().getParentBlock(), AEgen);
        
        // Iteratively solve dataflow equations
        while(!changed.isEmpty()) {   
            Node currentNode = changed.iterator().next();   
            changed.remove(currentNode);
            
            // Available in = intersect parents
            AvailableExpression AEin_n = null;
            for (Node parent : currentNode.getParents()) {
                AvailableExpression AEout_p = AEout.get(parent);
                if (AEin_n == null) {
                    AEin_n = new AvailableExpression(AEout_p);
                } else {
                    AEin_n.intersect(AEout_p);
                }
            }
            AEin.put(currentNode.getParentBlock(), AEin_n);
            
            // Available out = Generated union (In-Killed)            
            currentNode.accept(GEN);
            currentNode.accept(DEF);
            AvailableExpression AEgen_n = new AvailableExpression(currentNode.getParentBlock(), GEN.getGenExpression());
            AvailableExpression AEout_n = new AvailableExpression(AEin_n);
            for (IrIdentifier def : DEF.getDefinitions()) {
                AEout_n.removeExpression(def);
            }
            AEout_n.union(AEgen_n);       
            
            // Re-iterate if changed         
            if (!AEout_n.equals(AEout.get(currentNode.getParentBlock()))) {
                for (Node child : currentNode.getChildren()) {
                    if (child != null) {
                        changed.add(child);
                    }
                }
            }
            AEout.put(currentNode.getParentBlock(), AEout_n);
        }
    
    }
    
    
    
    
    
    

    private class LocalCSE implements NodeVisitor<Boolean> {
        
        private AEB aeb;
        private int tmpStart;
        private List<IrIdentifier> newTmps;
        
        public LocalCSE(int tmpStart) {
            this.tmpStart = tmpStart;
            this.newTmps = new ArrayList<IrIdentifier>();
        }
        
        public List<IrIdentifier> getNewTmps() {
            return this.newTmps;
        }

        @Override
        public Boolean visit(CfgBlock node) {
            this.aeb = new AEB(this.tmpStart);
            boolean mod = false;
            for (Node blkNode : node.getBlockNodes()) {
                mod |= blkNode.accept(this);
            }
            this.tmpStart = aeb.getTmpStart();
            this.newTmps = aeb.getNewTmps();
            return mod;
        }

        @Override
        public Boolean visit(CfgCondBranch node) {
            
            IrExpression exp = node.getExp();
            // Perform CSE
            boolean check = false;
            if (!aeb.available(exp)) {      // Expression is not available --> add
                aeb.addExpr(exp, node);
            } else if (!aeb.isNull(exp)) {  // Expression is available and in use --> replace
                node.setExp(aeb.getTmp(exp));
                check = true;
            } else {                        // Expression is available and not in use --> add temporary and replace
                aeb.addTmp(exp);
                IrIdentifier tmp = aeb.getTmp(exp);
                Node origin = aeb.getNode(exp);

                CfgStatement newNode = new CfgStatement(new IrAssignment(tmp, IrAssignment.IrAssignmentOp.ASSIGN, exp));
                newNode.setParentBlock(node.getParentBlock());
                origin.getParentBlock().prepend(origin, newNode);

                origin.setExp(tmp);
                node.setExp(tmp);
                check = true;
            }
            
            return check;
        }

        @Override
        public Boolean visit(CfgEntryNode node) {
            return false;
        }

        @Override
        public Boolean visit(CfgExitNode node) {
            return false;
        }

        @Override
        public Boolean visit(CfgStatement node) {
            
            // Skip on non-assignments (and destroy available expressions on method call)
            if (!node.getStatement().isAssignment()) {
                if (node.getStatement().isInvokeStatement()) {
                    IrInvokeStatement stat = (IrInvokeStatement) node.getStatement();
                    if (stat.getMethod().getExpKind() == IrExpression.expKind.METH) {
                        aeb.reset();
                    }
                }
                return false;
            }
                        
            IrAssignment ass = (IrAssignment)node.getStatement();
            IrExpression exp = ass.getExpression();
            
            // Reset on method call and ignore atomic expressions
            boolean skipCSE = false;
            switch (exp.getExpKind()) {
            case METH:
                aeb.reset();
            case BOOL:
            case CALL:
            case ID:
            case INT:
            case STRING:
                skipCSE = true;
            case BIN:
            case UN:
                break;
            default:
                throw new Error("unexpected type");
            }
            
            // Perform CSE
            boolean check = false;
            if (!skipCSE) {
                if (!aeb.available(exp)) {      // Expression is not available --> add
                    aeb.addExpr(exp, node);
                } else if (!aeb.isNull(exp)) {  // Expression is available and in use --> replace
                    ass.setExpression(aeb.getTmp(exp));
                    check = true;
                } else {                        // Expression is available and not in use --> add temporary and replace
                    aeb.addTmp(exp);
                    IrIdentifier tmp = aeb.getTmp(exp);
                    Node origin = aeb.getNode(exp);

                    CfgStatement newNode = new CfgStatement(new IrAssignment(tmp, IrAssignment.IrAssignmentOp.ASSIGN, exp));
                    newNode.setParentBlock(node.getParentBlock());
                    origin.getParentBlock().prepend(origin, newNode);

                    origin.setExp(tmp);
                    ass.setExpression(tmp);
                    check = true;
                }
            }
            
            // Clear re-assigned variables
            IrIdentifier var = ass.getLocation();
            aeb.clear(var);
            
            return check;
        }
    }
    
    
    private class AEB {
        
        private Map<IrExpression, IrIdentifier> expToTmpMap;
        private Map<IrExpression, Node> expToNodeMap;
        private List<IrIdentifier> newTmps;
        private int tmpStart;
        
        public int getTmpStart() {
            return this.tmpStart;
        }
        
        public List<IrIdentifier> getNewTmps() {
            return this.newTmps;
        }
        
        public AEB(int tmpStart) {
            this.expToTmpMap = new HashMap<IrExpression, IrIdentifier>();
            this.expToNodeMap = new HashMap<IrExpression, Node>();
            this.newTmps = new ArrayList<IrIdentifier>();
            this.tmpStart = tmpStart;
        }
        
        public void addExpr(IrExpression exp, Node node) {
            expToTmpMap.put(exp, null);
            expToNodeMap.put(exp, node);
        }
        
        public boolean available(IrExpression exp) {
            return expToTmpMap.containsKey(exp);
        }
        
        public boolean isNull(IrExpression exp) {
            if (!available(exp)) {
                throw new Error("Cannot search non-available expression");
            }
            return expToTmpMap.get(exp) == null;
        }
        
        public IrIdentifier getTmp(IrExpression exp) {
            if (!available(exp) || isNull(exp)) {
                throw new Error("No available temporary found");
            }
            return expToTmpMap.get(exp);
        }
        
        public Node getNode(IrExpression exp) {
            if (!available(exp)) {
                throw new Error("No corresponding node found");
            }
            return expToNodeMap.get(exp);
        }
        
        public void addTmp(IrExpression exp) {
            if (!available(exp)) {
                throw new Error("No corresponding exp found");
            }
            IrIdentifier tmp = new IrIdentifier(getTmpName());
            tmp.setExpType(exp.getExpType());
            newTmps.add(tmp);
            expToTmpMap.put(exp, tmp);
        }
        
        private String getTmpName() {
            return "_tmp" + this.tmpStart++;
        }
        
        public void reset() {
            this.expToTmpMap = new HashMap<IrExpression, IrIdentifier>();
            this.expToNodeMap = new HashMap<IrExpression, Node>();
        }
        
        public void clear(IrIdentifier var) {
            Iterator<IrExpression> it = expToNodeMap.keySet().iterator();
            while (it.hasNext()) {
                IrExpression exp = it.next();
                if (exp.contains(var)) {
                    it.remove();
                }
            }
        }
        
    }
    
    
    // Available Expression set
    private class AvailableExpression {
        
        private Map<IrExpression, IrIdentifier> expToTmpMap;
        private Map<IrExpression, ExpressionLocation> expToLocationMap;
        
        public AvailableExpression() {
            this.expToLocationMap = new HashMap<IrExpression, ExpressionLocation>();
            this.expToTmpMap = new HashMap<IrExpression, IrIdentifier>();
        }
        
        public AvailableExpression(AvailableExpression that) {
            this.expToLocationMap = new HashMap<IrExpression, ExpressionLocation>(that.expToLocationMap);
            this.expToTmpMap = new HashMap<IrExpression, IrIdentifier>(that.expToTmpMap);
        }
        
        public AvailableExpression(CfgBlock block, Map<IrExpression, Node> expMap) {
            this.expToLocationMap = new HashMap<IrExpression, ExpressionLocation>();
            this.expToTmpMap = new HashMap<IrExpression, IrIdentifier>();
            this.addExpression(block, expMap);
        }

        public void addExpression(CfgBlock block, Map<IrExpression, Node> expMap) {
            for (IrExpression exp : expMap.keySet()) {
                expToLocationMap.put(exp, new ExpressionLocation(block, expMap.get(exp)));
                expToTmpMap.put(exp, null);
            }
        }   
        
        public void removeExpression(IrIdentifier id) {
            Iterator<IrExpression> it = this.expToLocationMap.keySet().iterator();
            while (it.hasNext()) {
                IrExpression exp = it.next();
                if (exp.contains(id)) {
                    it.remove();
                }
            }
        }
        
        public void intersect(AvailableExpression that) {
            Iterator<IrExpression> it = this.expToLocationMap.keySet().iterator();
            while (it.hasNext()) {
                IrExpression exp = it.next();
                if (!that.expToLocationMap.containsKey(exp)) {
                    this.expToTmpMap.remove(exp);
                    it.remove();
                }
            }
        }
        
        public void union(AvailableExpression that) {
            for (IrExpression exp : that.expToLocationMap.keySet()) {
                if (this.expToLocationMap.containsKey(exp)) {
                    for (BlockLocation location : that.expToLocationMap.get(exp).getLocations()) {
                        this.expToLocationMap.get(exp).addLocation(location.getBlock(), location.getNode());
                    }
                } else {
                    this.expToLocationMap.put(exp, that.expToLocationMap.get(exp));
                }
                this.expToTmpMap.put(exp, that.expToTmpMap.get(exp));
            }
        }
        
        @Override
        public boolean equals(Object that) {
            if (!(that instanceof AvailableExpression)) {
                return false;
            }
            AvailableExpression thatAE = (AvailableExpression)that;
            
            for (IrExpression exp : thatAE.expToLocationMap.keySet()) {
                if (!this.expToLocationMap.containsKey(exp)) {
                    return false;
                }
            }
            return thatAE.expToLocationMap.size() == this.expToLocationMap.size();
        }
        
        @Override
        public String toString() {
            String str = "[";
            for (IrExpression exp : this.expToLocationMap.keySet()) {
                str += exp.toString() + " ";
            }
            return str + "]";
        }
    }    
    
    // Available expressions location (block and statement)
    private class ExpressionLocation {
        
        private List<BlockLocation> location;
        
        public ExpressionLocation(CfgBlock block, Node node) {
            this.location = new ArrayList<BlockLocation>();
            this.addLocation(block, node);
        }
        
        public void addLocation(CfgBlock block, Node node) {
            this.location.add(new BlockLocation(block, node));
        }
        
        public List<BlockLocation> getLocations() {
            return location;
        }
        
    }
    
    private class BlockLocation {
        private CfgBlock block;
        private Node node;
        
        public BlockLocation(CfgBlock block, Node node) {
            this.block = block;
            this.node = node;
        }
        
        public CfgBlock getBlock() {
            return this.block;
        }
        
        public Node getNode() {
            return this.node;
        }
    }
    
}
