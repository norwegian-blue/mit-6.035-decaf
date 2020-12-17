package cfg.Optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.*;
import ir.Statement.IrAssignment;

/**
 * @author Nicola
 */

public class CSE {
    
    private int tmpStart;
    private List<IrIdentifier> newTmps;
    
    public CSE(int tmpStart) {
        this.tmpStart = tmpStart;
        this.newTmps = new ArrayList<IrIdentifier>();
    }
    
    public List<IrIdentifier> getNewTmps() {
        return this.newTmps;
    }
    
    public boolean doCSE(MethodCFG cfg){
        
        boolean change = false;
        
        // Local
        LocalCSE local = new LocalCSE(this.tmpStart);        
        for (Node block : cfg.getNodes()) {
            change |= block.accept(local);
            this.newTmps.addAll(local.getNewTmps());
        }
        
        return change;
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
            // TODO Auto-generated method stub
            return false;
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
            
            // Skip on non-assignments
            // TODO handle method call
            if (!node.getStatement().isAssignment()) {
                return false;
            }
                        
            IrAssignment ass = (IrAssignment)node.getStatement();
            IrExpression exp = ass.getExpression();
            
            // Skip call
            // TODO handle method calls and others
            if (exp.getExpKind() != IrExpression.expKind.BIN) {
                return false;
            }
            
            // Perform CSE
            if (!aeb.available(exp)) {      // Expression is not available --> add
                aeb.addExpr(exp, node);
                return false;
            } else if (!aeb.isNull(exp)) {  // Expression is available and in use --> replace
                ass.setExpression(aeb.getTmp(exp));
                return true;
            } else {                        // Expression is available and not in use --> add temporary and replace
                aeb.addTmp(exp);
                IrIdentifier tmp = aeb.getTmp(exp);
                Node origin = aeb.getNode(exp);
                
                CfgStatement newNode = new CfgStatement(new IrAssignment(tmp, IrAssignment.IrAssignmentOp.ASSIGN, exp));
                newNode.setParentBlock(node.getParentBlock());
                origin.getParentBlock().prepend(origin, newNode);
                
                origin.setExp(tmp);
                ass.setExpression(tmp);
                return true;
            }
            
            
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
            
        
    }
    
}
