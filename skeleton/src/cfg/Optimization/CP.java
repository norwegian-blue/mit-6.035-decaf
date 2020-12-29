package cfg.Optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.*;
import ir.Statement.*;
import semantic.BaseTypeDescriptor;
import semantic.LocalDescriptor;

public class CP {
    
    private Map<CfgBlock, AvailableCopyInstruction> ACPin;
    private Map<CfgBlock, AvailableCopyInstruction> ACPout;
    private List<LocalDescriptor> locals;
    private boolean CP_db = false;
    
    public CP(List<LocalDescriptor> locals) {
        this.ACPin = new HashMap<CfgBlock, AvailableCopyInstruction>();
        this.ACPout = new HashMap<CfgBlock, AvailableCopyInstruction>();
        this.locals = locals;
    }
    
    public boolean optimize(MethodCFG cfg){
        
        boolean change = false;
        CP_Block cp = new CP_Block();  
        
        // Local
        for (Node block : cfg.getNodes()) {
            if (cfg.getRoot().equals(block)) {
                cp.initializeAvailableInstructions(locals);     // All locals initialized to 0
            } else {
                cp.resetAvailableInstructions();                // No values available
            }
            change |= block.accept(cp);
        }
            
        // Get available copy instructions
        getReachingDefinitions(cfg);
        
        if (CP_db) {
            System.out.println("!!!!!!!!!! ACP in !!!!!!!!!!!");
            System.out.println(this.ACPin);
            System.out.println("!!!!!!!!!! ACP out !!!!!!!!!!!");
            System.out.println(this.ACPout);        
        }
        
        // Global       
        for (Node block : cfg.getNodes()) {
            cp.setAvailableInstructions(ACPin.get(block));      // Get available copy instructions for dataflow analysis
            change |= block.accept(cp);
        }
        
        return change;  
    }    
    
        
    private void getReachingDefinitions(MethodCFG cfg) {
     
        // Initialize available outlet/inlet expressions to empty set for all blocks
        for (Node block : cfg.getNodes()) {
            ACPin.put(block.getParentBlock(), new AvailableCopyInstruction());
            ACPout.put(block.getParentBlock(), new AvailableCopyInstruction());
        }
        
        // Process entry block
        CP_Block cp = new CP_Block();
        PropagationFinder propFind = new PropagationFinder();
        cp.initializeAvailableInstructions(this.locals);
        ACPin.replace(cfg.getRoot().getParentBlock(), cp.getAvailableCopyInstruction());
        ACPout.replace(cfg.getRoot().getParentBlock(), propFind.find(cfg.getRoot().getParentBlock(), ACPin.get(cfg.getRoot())));
                
        // Initialize list of blocks
        Set<Node> changed = new HashSet<Node>(cfg.getNodes());
        changed.remove(cfg.getRoot());       
                
        // Iteratively solve dataflow equations
        while(!changed.isEmpty()) {   
            Node currentNode = changed.iterator().next();   
            changed.remove(currentNode);
            
            // Available in = intersect parents
            AvailableCopyInstruction ACPin_n = null;
            for (Node parent : currentNode.getParents()) {
                AvailableCopyInstruction ACPout_p = ACPout.get(parent);
                if (ACPin_n == null) {
                    ACPin_n = new AvailableCopyInstruction(ACPout_p);
                } else {
                    ACPin_n.intersect(ACPout_p);
                }
            }
            ACPin.put(currentNode.getParentBlock(), ACPin_n);
            
            if (CP_db) {
                System.out.println("####### ACP in " + currentNode.getParentBlock().getBlockName() + " ############\n");
                System.out.println(ACPin_n);
            }
            
            // Available out = Copied union (In-Killed)            
            AvailableCopyInstruction ACPout_n = propFind.find(currentNode.getParentBlock(), ACPin.get(currentNode));     
            
            if (CP_db) {
                System.out.println("####### ACP out " + currentNode.getParentBlock().getBlockName() + " ############\n");
                System.out.println(ACPout_n);
            
                System.out.println("####### ACP out prev " + currentNode.getParentBlock().getBlockName() + " ############\n");
                System.out.println(ACPout.get(currentNode.getParentBlock()));
            }
                
            // Re-iterate if changed         
            if (!ACPout_n.equals(ACPout.get(currentNode.getParentBlock()))) {
                if (CP_db) System.out.println("repeat\n");
                for (Node child : currentNode.getChildren()) {
                    if (child != null) {
                        changed.add(child);
                    }
                }
            }
            
            
            
            ACPout.replace(currentNode.getParentBlock(), ACPout_n);
        }
    
    }
    
    private class PropagationFinder implements NodeVisitor<Void> {
        
        private AvailableCopyInstruction acp;
        
        public AvailableCopyInstruction find(CfgBlock block, AvailableCopyInstruction acpIn) {
            this.acp = new AvailableCopyInstruction(acpIn);
            block.accept(this);
            return this.acp;
        }

        @Override
        public Void visit(CfgBlock node) {
            for (Node blkNode : node.getBlockNodes()) {
                blkNode.accept(this);
            }
            return null;
        }

        @Override
        public Void visit(CfgCondBranch node) {
            return null;
        }

        @Override
        public Void visit(CfgEntryNode node) {
            return null;
        }

        @Override
        public Void visit(CfgExitNode node) {
            return null;
        }

        @Override
        public Void visit(CfgStatement node) {
            
            // Skip on non-assignments
            if (!node.getStatement().isAssignment()) {
                return null;
            }
            
            IrAssignment ass = (IrAssignment)node.getStatement();
            IrIdentifier id = ass.getLocation();
            IrExpression exp = ass.getExpression();
                       
            // Replace assigned variable
            switch(exp.getExpKind()) {
            case ID:
                if (!exp.isAtom()) {
                    this.acp.removeCopyInstruction(id);
                    break;
                }
            case BOOL:
            case INT:
                this.acp.addCopyInstruction(id, exp);
                break;
            default:
                this.acp.removeCopyInstruction(id);
                break;
            }
            
            return null;

        }
    }
    
    
    private class CP_Block implements NodeVisitor<Boolean> {
        
        private AvailableCopyInstruction acp;
        
        public CP_Block() {
            this.acp = new AvailableCopyInstruction();
        }
        
        public void resetAvailableInstructions() {
            this.acp = new AvailableCopyInstruction();
        }
        
        public void setAvailableInstructions(AvailableCopyInstruction acp) {
            this.acp = acp;
        }
        
        public void initializeAvailableInstructions(List<LocalDescriptor> locals) {
            this.resetAvailableInstructions();
            for (LocalDescriptor local : locals) {
                IrIdentifier id = new IrIdentifier(local.getId());
                id.setExpType(local.getType());
                if (local.isGlobal()) {
                    continue;
                }
                if (local.getType().equals(BaseTypeDescriptor.BOOL)) {
                    this.acp.addCopyInstruction(id, new IrBooleanLiteral("false"));
                } else {
                    this.acp.addCopyInstruction(id, new IrIntLiteral("0"));
                }
            }
        }
        
        public AvailableCopyInstruction getAvailableCopyInstruction() {
            return this.acp;
        }
        
        public boolean copyPropagation(IrExpression exp) {
            boolean check = false;
            
            switch(exp.getExpKind()) {
            case BIN:
                IrBinaryExpression binExp = (IrBinaryExpression) exp;
                if (acp.available(binExp.getLHS())) {
                    binExp.setLHS(acp.getExp(binExp.getLHS()));
                    check = true;
                }
                if (acp.available(binExp.getRHS())) {
                    binExp.setRHS(acp.getExp(binExp.getRHS()));
                    check = true;
                }
                break;
            case UN:
                IrUnaryExpression unExp = (IrUnaryExpression) exp;
                if (acp.available(unExp.getExp())) {
                    unExp.setExp(acp.getExp(unExp.getExp()));
                    check = true;
                }
                break;
            case CALL:
            case METH:
                IrCallExpression callExp = (IrCallExpression) exp;
                for (int i = 0; i < callExp.getArgs().size(); i++) {
                    if (acp.available(callExp.getArgs().get(i))) {
                        callExp.getArgs().set(i, acp.getExp(callExp.getArgs().get(i)));
                        check = true;
                    }
                }
            default:
                break;
            }
            
            return check;
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
            
            // Perform Copy Propagation
            if (exp.getExpKind().equals(IrExpression.expKind.ID)) {
                if (acp.available(exp)) {
                    node.setExp(acp.getExp(exp));
                    return true;
                }
                return false;
            } else {           
                return this.copyPropagation(exp);
            }
            
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
                    boolean check = this.copyPropagation(stat.getMethod());
                    return check;
                }
                return false;
            }
            
            IrAssignment ass = (IrAssignment)node.getStatement();
            IrIdentifier id = ass.getLocation();
            IrExpression exp = ass.getExpression();
                       
            // Perform copy propagation
            boolean check = this.copyPropagation(exp);
            if (exp.getExpKind().equals(IrExpression.expKind.ID)) {
                if (acp.available(exp)) {
                    ass.setExpression(acp.getExp(exp));
                    check = true;
                }
            }
                
            if (ass.getLocation().isArrayElement()) {
                if (acp.available(id.getInd())) {
                    id.setIndex(acp.getExp(id.getInd()));
                    check = true;
                }
            }
            
            // Replace assigned variable
            switch(exp.getExpKind()) {
            case ID:
                if (!exp.isAtom()) {
                    this.acp.removeCopyInstruction(id);
                    break;
                }
            case BOOL:
            case INT:
                this.acp.addCopyInstruction(id, exp);
                break;
            default:
                this.acp.removeCopyInstruction(id);
                break;
            }
            
            return check;
        }
    }
    
     
    // Available Expression set
    private class AvailableCopyInstruction {
        
        private Map<IrIdentifier, IrExpression> idToExpMap;
        
        public AvailableCopyInstruction() {
            this.idToExpMap = new HashMap<IrIdentifier, IrExpression>();
        }
        
        public AvailableCopyInstruction(AvailableCopyInstruction acp) {
            this.idToExpMap = new HashMap<IrIdentifier, IrExpression>(acp.idToExpMap);
        }
        
        public boolean available(IrExpression id) {
            if (id.getExpKind().equals(IrExpression.expKind.ID)) {
                return this.idToExpMap.containsKey(id) && this.idToExpMap.get(id)!=null;
            } else {
                return false;
            }
        }
        
        private boolean weakAvailable(IrExpression id) {
            if (id.getExpKind().equals(IrExpression.expKind.ID)) {
                return this.idToExpMap.containsKey(id);
            } else {
                return false;
            }
        }

        public void addCopyInstruction(IrExpression expId, IrExpression value) {
            IrIdentifier id = (IrIdentifier) expId;
            
            // Skip globals
            if (id.getId().startsWith("_glb")) {
                return;
            }
            
            // Skip if self assignemnt
            if (expId.equals(value)) {
                return;
            }
            
            // Skip if reverted assignment already exists
            if (value.getExpKind().equals(IrExpression.expKind.ID)) {
                IrExpression valueId = (IrExpression) value;
                if (available(valueId) && id.equals(this.getExp(valueId))){
                    return;
                }
            }
            
            if (this.weakAvailable(id)) {
                this.idToExpMap.replace(id, value);
            } else {
                this.idToExpMap.put(id, value);
            }
        }
        
        public void removeCopyInstruction(IrIdentifier idEx) {
            
            for(IrIdentifier id : this.idToExpMap.keySet()) {
                if (!this.available(id)) continue;
                if (id.equals(idEx) || this.idToExpMap.get(id).equals(idEx)) {
                    this.idToExpMap.replace(id, null);
                }
            }
            
            if (!this.idToExpMap.containsKey(idEx)) {
                this.idToExpMap.put(idEx, null);
            }
        }
        
        public IrExpression getExp(IrExpression expId) {
            IrIdentifier id = (IrIdentifier) expId;
            if (this.available(id)) {
                return this.idToExpMap.get(id);
            } else {
                throw new Error("Temporary is not available");
            }
        }
        
        public void intersect(AvailableCopyInstruction that) {
            
            //System.out.println(this);
            //System.out.println(that);
            
            // Merge copy if in both sets or add if missing
            for (IrIdentifier thatId : that.idToExpMap.keySet()) {
                
                //System.out.println(thatId);
                
                if (!that.available(thatId)) {
                    //System.out.println("\tthat not available");
                    this.idToExpMap.put(thatId, null);
                    this.idToExpMap.replace(thatId, null);
                    continue;
                }
                
                if (this.available(thatId)) {
                    if (!that.getExp(thatId).equals(this.getExp(thatId))) {
                        this.removeCopyInstruction(thatId);
                    }
                } else {
                    if(!this.idToExpMap.containsKey(thatId)) {
                        this.addCopyInstruction(thatId, that.getExp(thatId));
                    }
                }
            }
            
          }
                    
        @Override
        public int hashCode() {
            return 0;
        }
                
        @Override
        public boolean equals(Object thatObj) {

            if (!(thatObj instanceof AvailableCopyInstruction)) {
                return false;
            }
            AvailableCopyInstruction that = (AvailableCopyInstruction)thatObj;
            
            for (IrIdentifier exp : that.idToExpMap.keySet()) {
                if (!this.idToExpMap.containsKey(exp)) {
                    return false;
                } else if (this.available(exp) != that.available(exp)) {
                    return false;
                }
            }
            return that.idToExpMap.size() == this.idToExpMap.size();
        }
        
        @Override
        public String toString() {
            String str = "[";
            for (IrIdentifier id : this.idToExpMap.keySet()) {
                if (this.available(id)) {
                    str += id.toString() + "-->" + this.idToExpMap.get(id).toString() + " ";
                } else {
                    str += id.toString() + "-->null ";
                }
            }
            return str + "]\n\n";
        }
    }     
}

