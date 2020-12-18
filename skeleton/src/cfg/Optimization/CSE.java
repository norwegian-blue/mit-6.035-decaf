package cfg.Optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private HashMap<CfgBlock, ExpressionLocation> AEin;
    private HashMap<CfgBlock, ExpressionLocation> AEout;
    
    public CSE(int tmpStart) {
        this.tmpStart = tmpStart;
        this.newTmps = new ArrayList<IrIdentifier>();
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
    
    private void getAvailableExpressions(MethodCFG cfg) {
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
            for (IrExpression exp : expToNodeMap.keySet()) {
                if (exp.contains(var)) {
                    expToNodeMap.remove(exp);
                    expToTmpMap.remove(exp);
                }
            }
        }
        
    }
    
    
    // Available expressions location (block and statement)
    private class ExpressionLocation {
        
        private IrIdentifier tmp;
        private List<BlockLocation> location;
        
        public ExpressionLocation(CfgBlock block, Node node) {
            this.location.add(new BlockLocation(block, node));
        }
        
        public void setTmp(IrIdentifier tmp) {
            this.tmp = tmp;
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
    
}
