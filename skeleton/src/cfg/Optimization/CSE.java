package cfg.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.*;
import ir.Statement.IrAssignment;
import ir.Statement.IrInvokeStatement;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */

public class CSE {
    
    private int tmpStart;
    private Set<IrIdentifier> newTmps;
    
    private Map<CfgBlock, AvailableExpression> AEin;
    private Map<CfgBlock, AvailableExpression> AEout;
    
    public CSE(int tmpStart) {
        this.tmpStart = tmpStart;
        this.newTmps = new HashSet<IrIdentifier>();
        this.AEin = new HashMap<CfgBlock, AvailableExpression>();
        this.AEout = new HashMap<CfgBlock, AvailableExpression>();
    }
    
    public Set<IrIdentifier> getNewTmps() {
        return this.newTmps;
    }
    
    public boolean optimize(MethodCFG cfg){
        
        boolean change = false;
        CSE_Block cse = new CSE_Block(this.tmpStart);  
        
        // Local
        for (Node block : cfg.getNodes()) {
            cse.resetAvailableExpression();    // No initial guess available
            change |= block.accept(cse);
        }        
        
        // Get available expressions
        getAvailableExpressions(cfg);
        
        // Global       
        for (Node block : cfg.getNodes()) {
            cse.setAvailableExpression(AEin.get(block));    // Get available expression for dataflow analysis
            change |= block.accept(cse);
            this.newTmps.addAll(cse.getNewTmps());
        }
        
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
    
    
    private class CSE_Block implements NodeVisitor<Boolean> {
        
        private AvailableExpression aeb;
        private int tmpStart;
        private Set<IrIdentifier> newTmps;
        
        public CSE_Block(int tmpStart) {
            this.tmpStart = tmpStart;
            this.newTmps = new HashSet<IrIdentifier>();
            this.aeb = new AvailableExpression();
        }
        
        public void setAvailableExpression(AvailableExpression aeb) {
            this.aeb = aeb;
        }
        
        public void resetAvailableExpression() {
            this.aeb = new AvailableExpression();
        }
        
        public Set<IrIdentifier> getNewTmps() {
            return this.newTmps;
        }
        
        public IrIdentifier getNewTmp(TypeDescriptor type) {
            String tmpName = "_tmp" + this.tmpStart++;
            IrIdentifier tmp = new IrIdentifier(tmpName);
            tmp.setExpType(type);
            return tmp;
        }

        @Override
        public Boolean visit(CfgBlock node) {
            boolean mod = false;
            for (Node blkNode : node.getBlockNodes()) {
                mod |= blkNode.accept(this);
            }
            return mod;
        }

        @Override
        public Boolean visit(CfgCondBranch node) {
            
            IrExpression exp = node.getExp();
            
            // Reset available expressions if call
            switch (exp.getExpKind()) {
            case CALL:
            case METH:
                this.resetAvailableExpression();
                return false;
            default:
                break;
            }
                       
            // Perform CSE
            boolean check = false;
            if (!aeb.available(exp)) {      // Expression is not available --> add
                aeb.addExpression(exp, node.getParentBlock(), node);
            } else if (aeb.tmpAvailable(exp)) {  // Expression is available and in use --> replace
                node.setExp(aeb.getTmp(exp));
                check = true;
            } else {                        // Expression is available and not in use --> add temporary and replace
                IrIdentifier tmp = getNewTmp(exp.getExpType());
                this.newTmps.add(tmp);
                aeb.addTmp(exp, tmp);
                
                // Update CS origin nodes   a = b+c;    -->     tmp = b+c;  a = tmp;
                for (BlockLocation location : aeb.getLocation(exp)) {
                    CfgBlock originBlock = location.getBlock();
                    Node origin = location.getNode();
                    
                    // Avoid replacing self
                    if (origin.equals(node)) {
                        continue;
                    }
                    
                    // Get origin expression (may have been reassigned already)
                    IrExpression originExp = origin.getExp();
                    
                    CfgStatement newNode = new CfgStatement(new IrAssignment(tmp, IrAssignment.IrAssignmentOp.ASSIGN, originExp));
                    newNode.setParentBlock(originBlock);
                    origin.getParentBlock().prepend(origin, newNode);
                    origin.setExp(tmp);
                }
                               
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
                        this.resetAvailableExpression();
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
                this.resetAvailableExpression();
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
                if (!aeb.available(exp)) {              // Expression is not available --> add
                    aeb.addExpression(exp, node.getParentBlock(), node);
                } else if (aeb.tmpAvailable(exp)) {     // Expression is available and in use --> replace
                    ass.setExpression(aeb.getTmp(exp));
                    check = true;
                } else {                                // Expression is available and not in use --> add temporary and replace
                    IrIdentifier tmp = getNewTmp(exp.getExpType());
                    this.newTmps.add(tmp);
                    aeb.addTmp(exp, tmp);
                    
                    // Update CS origin nodes   a = b+c;    -->     tmp = b+c;  a = tmp;
                    for (BlockLocation location : aeb.getLocation(exp)) {
                        CfgBlock originBlock = location.getBlock();
                        Node origin = location.getNode();
                        
                        // Avoid replacing self
                        if (origin.equals(node)) {
                            continue;
                        }
                        
                        // Get origin expression (may have been reassigned already)
                        IrExpression originExp = origin.getExp();
                        
                        CfgStatement newNode = new CfgStatement(new IrAssignment(tmp, IrAssignment.IrAssignmentOp.ASSIGN, originExp));
                        newNode.setParentBlock(originBlock);
                        origin.getParentBlock().prepend(origin, newNode);
                        origin.setExp(tmp);
                    }
                    
                    // Reassign current expression to new tmp
                    ass.setExpression(tmp);
                    check = true;
                }
            }
            
            // Clear re-assigned variables
            IrIdentifier var = ass.getLocation();
            aeb.removeExpression(var);
            
            return check;
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
                addExpression(exp, block, expMap.get(exp));
            }
        }
        
        public void addExpression(IrExpression exp, CfgBlock block, Node node) {
            expToLocationMap.put(exp, new ExpressionLocation(block, node));
            expToTmpMap.put(exp, null);
        }
        
        public Set<BlockLocation> getLocation(IrExpression exp) {
            return this.expToLocationMap.get(exp).getLocations();
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
        
        public boolean available(IrExpression exp) {
            return this.expToLocationMap.containsKey(exp);
        }
        
        public boolean tmpAvailable(IrExpression exp) {
            if (this.available(exp)) {
                return !(this.expToTmpMap.get(exp) == null);
            } else {
                throw new Error("Expression is not available");
            }
        }
        
        public IrIdentifier getTmp(IrExpression exp) {
            if (this.tmpAvailable(exp)) {
                return this.expToTmpMap.get(exp);
            } else {
                throw new Error("Temporary is not available");
            }
        }
        
        public void addTmp(IrExpression exp, IrIdentifier tmp) {
            this.expToTmpMap.put(exp, tmp);
        }
        
        public void intersect(AvailableExpression that) {
            Iterator<IrExpression> it = this.expToLocationMap.keySet().iterator();
            // Remove if not in both sets
            while (it.hasNext()) {
                IrExpression exp = it.next();
                if (!that.expToLocationMap.containsKey(exp)) {
                    this.expToTmpMap.remove(exp);
                    it.remove();
                }                
            }
            // Merge locations if in both sets
            for (IrExpression thatExp : that.expToLocationMap.keySet()) {
                if (this.expToLocationMap.containsKey(thatExp)) {
                    for (BlockLocation location : that.expToLocationMap.get(thatExp).getLocations()) {
                        this.expToLocationMap.get(thatExp).addLocation(location.getBlock(), location.getNode());
                    }
                }
            }
        }
        
        public void union(AvailableExpression that) {
            for (IrExpression exp : that.expToLocationMap.keySet()) {
                if (this.expToLocationMap.containsKey(exp)) {
                    this.expToLocationMap.replace(exp, that.expToLocationMap.get(exp));
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
        
        private Set<BlockLocation> location;
        
        public ExpressionLocation(CfgBlock block, Node node) {
            this.location = new HashSet<BlockLocation>();
            this.addLocation(block, node);
        }
        
        public void addLocation(CfgBlock block, Node node) {
            this.location.add(new BlockLocation(block, node));
        }
        
        public Set<BlockLocation> getLocations() {
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
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        @Override
        public boolean equals(Object that) {
            if (!(that instanceof BlockLocation)) {
                return false;
            }
            BlockLocation thatBlock = (BlockLocation)that;
            return thatBlock.getNode().equals(this.getNode());
        }
        
    }
    
}
