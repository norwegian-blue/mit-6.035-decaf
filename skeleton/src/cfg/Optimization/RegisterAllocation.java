package cfg.Optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.IrBinaryExpression;
import ir.Expression.IrCallExpression;
import ir.Expression.IrExpression;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;
import ir.Statement.IrInvokeStatement;
import ir.Statement.IrStatement;
import semantic.MethodDescriptor;
import semantic.ParameterDescriptor;

/**
 * @author Nicola
 */
public class RegisterAllocation {
    
    private static MethodDescriptor currentMethod;

    public void allocate(MethodCFG cfg, MethodDescriptor method) {
        
        currentMethod = method;
                
        // Get DU chains from reaching definitions dataflow analysis
        Set<DuChain> duChains = new ReachingDefinitions(cfg).getDuChains();
        
        // Merge DU chains in Webs for register allocation  
        Set<Web> webs = Web.getWebs(duChains);
        
        // Build adjacency matrix from Webs
        InterferenceGraph graph = new InterferenceGraph(webs);
        
        // Compute spill costs
        graph.computeSpillCost();
        
        // Color graph
        graph.color();
        
        // Allocate registers to webs
        for (Web web : webs) {
            web.allocate(graph);
        }
        currentMethod.addWebs(webs);
        
        //System.out.println(graph);
        //System.out.println("WEBS :\n" + webs + "\n");
    }
    
    private class ReachingDefinitions {
        
        private Map<CfgBlock, RCH> RCHin;
        private Map<CfgBlock, RCH> RCHout;
        private List<CfgBlock> blocks;
        private Set<DuChain> duChains;
        
        public Set<DuChain> getDuChains() {
            return duChains;
        }
        
        public ReachingDefinitions(MethodCFG cfg) {
            
            // Do liveness analysis --> get parameters and variables live at method start
            new LivenessAnalysis().analyze(cfg);
            
            // Get method blocks
            blocks = new ArrayList<CfgBlock>();
            for (Node block : cfg.getNodes()) {
                blocks.add(block.getParentBlock());
            }
            
            // Initialize maps
            RCHin = new HashMap<CfgBlock, RCH>();
            RCHout = new HashMap<CfgBlock, RCH>();
            
            // Do dataflow analysis
            this.getReachingDefinitions();
        }
        
        private void getReachingDefinitions() {
            // Initialize RCHin/out + DuChains
            duChains = new HashSet<DuChain>();
            for (CfgBlock block : blocks) {
                RCHin.put(block, new RCH());
                RCHout.put(block, new RCH());
            }
                        
            // Iteratively solve dataflow problem
            Set<CfgBlock> changed = new HashSet<CfgBlock>(blocks);
            DefinitionsUpdate defUpd = new DefinitionsUpdate();
            
            while (!changed.isEmpty()) {
                CfgBlock currentBlock = changed.iterator().next();
                changed.remove(currentBlock);
                
                // Update RCHin_i --> union of parent RCH
                RCH RCHin_i = RCHin.get(currentBlock);
                for (Node parent : currentBlock.getParents()) {
                    RCHin_i.addAll(RCHout.get(parent.getParentBlock()));
                }
                
                // Update RCHout_i --> GEN_i + (RCHin_i - KILL_i)
                defUpd.process(currentBlock, RCHin_i, duChains);
                RCH RCHout_i = defUpd.getRCHout();
                this.duChains = defUpd.getChains();
                
                // Re-iterate if changed
                if (!RCHout_i.equals(RCHout.get(currentBlock))) {
                    for (Node child : currentBlock.getChildren()) {
                        if (child != null) {
                            changed.add(child.getParentBlock());
                        }
                    }
                }
                
                RCHout.replace(currentBlock, RCHout_i);
                
            }
            
        }
        
        private class RCH {
            private Set<Definition> defSet;
            
            public RCH() {
                defSet = new HashSet<Definition>();
            }
            
            public RCH(RCH that) {
                defSet = new HashSet<Definition>(that.defSet);
            }
            
            public void addAll(RCH that) {
                defSet.addAll(that.defSet);
            }
                        
            public void killDef(IrIdentifier id) {
                Iterator<Definition> it = defSet.iterator();
                while (it.hasNext()) {
                    Definition def = it.next();
                    if (def.getId().equals(id)) {
                        it.remove();
                    }
                }
            }
            
            public void addDef(Definition def) {
                defSet.add(def);
            }
            
            public Set<Definition> getDefSet(IrIdentifier id) {
                Set<Definition> idSet = new HashSet<Definition>();
                for (Definition def : defSet) {
                    if (def.getId().equals(id)) {
                        idSet.add(def);
                    }
                }
                return idSet;
            }
            
            @Override
            public String toString() {
                return defSet.toString();
            }
            
            @Override
            public boolean equals(Object thatObj) {
                if (!(thatObj instanceof RCH)) {
                    return false;
                }
                RCH that = (RCH) thatObj;
                return this.defSet.containsAll(that.defSet) && this.defSet.size()==that.defSet.size();
            }
            
        }

        private class Definition {
            private IrIdentifier id;
            private Node node;
            
            public Definition(IrIdentifier id, Node node) {
                this.id = id;
                this.node = node;
            }
            
            public IrIdentifier getId() {
                return this.id;
            }
            
            public Node getNode() {
                return this.node;
            }
            
            @Override
            public int hashCode() {
                return id.hashCode();
            }
            
            @Override
            public boolean equals(Object thatObj) {
                if (!(thatObj instanceof Definition)) {
                    return false;
                }
                Definition that = (Definition) thatObj;
                return (this.id.equals(that.id)) && (this.node.equals(that.node));
            }
            
            @Override
            public String toString() {
                return "{" + id.toString() + ", " + node.toString() + "}";
            }
        }
        
        private class DefinitionsUpdate implements NodeVisitor<Void> {
            
            private RCH RCHout;
            private Set<DuChain> chains;
            
            public void process(CfgBlock block, RCH RCHin, Set<DuChain> chains) {
                RCHout = new RCH(RCHin);
                this.chains = chains;
                block.accept(this);
            }
            
            public RCH getRCHout() {
                return this.RCHout;
            }
            
            public Set<DuChain> getChains() {
                return this.chains;
            }
            
            private void updateUses(Node node, Set<IrIdentifier> ids) {
                
                UD use = new UD(node.getParentBlock(), node);
                                
                for (IrIdentifier id : ids) {
                    // Get definitions of id
                    Set<Definition> defSet = RCHout.getDefSet(id);
                    
                    // Add uses to corresponding DUChain
                    for (Definition def : defSet) {
                        UD ud = new UD(def.getNode().getParentBlock(), def.getNode());
                        
                        for (DuChain chain : chains) {
                            if (chain.matches(id, ud)) {
                                chain.addUse(use);
                            }
                        }
                    }
                }
            }
            
            private void updateDefinitions(Node node, Set<IrIdentifier> ids) {
                UD def = new UD(node.getParentBlock(), node);
                for (IrIdentifier id : ids) {
                    DuChain newChain = new DuChain(id, def);
                    chains.add(newChain);
                }
            }
            
            private void updateDefinitions(Node node, IrIdentifier id) {
                Set<IrIdentifier> idSet = new HashSet<IrIdentifier>();
                idSet.add(id);
                this.updateDefinitions(node, idSet);
            }

            @Override
            public Void visit(CfgBlock node) {
                for (Node blockNode : node.getBlockNodes()) {
                    blockNode.accept(this);
                }
                return null;
            }

            @Override
            public Void visit(CfgCondBranch node) {
                
                // Update uses in available duChains
                this.updateUses(node, node.getExp().getUsedVars());
                
                return null;
            }

            @Override
            public Void visit(CfgEntryNode node) {
                
                // Update definitions in duChains
                this.updateDefinitions(node, node.getLiveVars());
                
                // Add variable live at start of method (parameters and local requiring to be 0 initialized)
                for (IrIdentifier id : node.getLiveVars()) {
                    RCHout.addDef(new Definition(id, node));
                }
                
                return null;
            }

            @Override
            public Void visit(CfgExitNode node) {
                if (node.returnsExp()) {
                    this.updateUses(node, node.getExp().getUsedVars());
                }
                return null;
            }

            @Override
            public Void visit(CfgStatement node) {
                // Update uses
                this.updateUses(node, node.getExp().getUsedVars());
                
                // Skip non-assignment
                if (!node.getStatement().isAssignment()) {
                    return null;
                }
                
                IrAssignment ass = (IrAssignment) node.getStatement();
                IrIdentifier id = (IrIdentifier) ass.getLocation();
                
                // Skip globals
                if (id.getId().startsWith("_glb")) {
                    if (id.isArrayElement()) {
                        this.updateUses(node, id.getInd().getUsedVars());
                    }
                    return null;
                }
                
                // OUT = GEN + (IN - KILL)
                Definition newDef = new Definition(id, node);
                RCHout.killDef(id);      // Kill old definition if present
                RCHout.addDef(newDef);   // Put new definition    
                
                // Update definitions
                this.updateDefinitions(node, id);
                
                return null;
            }
            
        }
    }
    
    private static class UD {
        private CfgBlock block;
        private Node node;
        
        public UD (CfgBlock block, Node node) {
            this.block = block;
            this.node = node;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        @Override
        public boolean equals(Object thatObj) {
            if (!(thatObj instanceof UD)) {
                return false;
            }
            UD that = (UD) thatObj;
            return block.equals(that.block) && node.equals(that.node);
        }
        
        @Override
        public String toString() {
            return block.getBlockName() + ": " + node.toString();
        }

        public Node getNode() {
            return this.node;
        }
        
    }
    
    private class DuChain {
        private IrIdentifier id;
        private UD definition;
        private Set<UD> uses;
        
        public DuChain(IrIdentifier id, UD definition) {
            this.id = id;
            this.definition = definition;
            this.uses = new HashSet<UD>();
        }
        
        public void addUse(UD use) {
            this.uses.add(use);
        }
        
        public IrIdentifier getId() {
            return id;
        }
        
        public UD getDef() {
            return this.definition;
        }
        
        public Set<UD> getUses() {
            return this.uses;
        }
        
        public boolean matches(IrIdentifier thatId, UD thatDef) {
            return id.equals(thatId) && definition.equals(thatDef);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
        @Override
        public boolean equals(Object thatObj) {
            if (!(thatObj instanceof DuChain)) {
                return false;
            }
            DuChain that = (DuChain) thatObj;
            return id.equals(that.id) && definition.equals(that.definition);
        }
        
        @Override
        public String toString() {
            String str =  "[" + id.toString() + ", " + definition.toString() + "] --> :";
            for (UD use : uses) {
                str += "\n\t " + use.toString();
            }
            return str + "\n";
        }

        public boolean hasCommonUses(DuChain that) {
            // Check only if same variable
            if (!that.id.equals(this.id)) return false;
            
            // Check if the two chains share a use
            for (UD use : that.uses) {
                if (this.uses.contains(use)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    public static class Web {
                
        private IrIdentifier id;
        private Set<UD> definitions;
        private Set<UD> uses;
        private Set<UD> liveRange;
        private boolean spilled;
        private int offset;
        private REG reg;
        private int symReg;
        
        private static int webNum;
        private static Set<Web> webs;
        
        private Set<Node> visited;
        private Stack<UD> range;
               
        public Web(IrIdentifier id, UD def, Set<UD> uses) {
            this.id = id;
            this.liveRange = new HashSet<UD>();
            this.definitions = new HashSet<UD>();
            this.definitions.add(def);
            this.uses = uses;
            this.spilled = false;
            this.offset = 0;
            this.symReg = ++webNum;
        }
        
        public void allocate(InterferenceGraph graph) {
            if (graph.canAllocate(this)) {
                reg = graph.getRegister(this);
            } else {
                this.spilled = true;
            }
        }

        public static Set<Web> getWebs(Set<DuChain> chains) {
            
            webNum = 0;
            webs = new HashSet<Web>();
            Stack<DuChain> stack = new Stack<DuChain>();
            stack.addAll(chains);
            
            while(!stack.isEmpty()) {
                DuChain chain = stack.pop();
                // skip globals
                if (chain.getId().getId().startsWith("_glb")) {
                    continue;
                }
                Web web = new Web(chain.getId(), chain.getDef(), chain.getUses());
                
                // Check if other chains can be merged into same web
                Iterator<DuChain> it = stack.iterator();
                while(it.hasNext()) {
                    DuChain otherChain = it.next();
                    if (chain.hasCommonUses(otherChain)) {
                        web.addChain(otherChain);
                        it.remove();
                    }
                }
                web.getLiveRange();
                webs.add(web);
            }

            return webs;
            
        }

        private void addChain(DuChain that) {
            // Add definition
            this.definitions.add(that.getDef());
            
            // Add uses
            this.uses.addAll(that.getUses());            
        }
        
        private void getLiveRange() {
            for (UD def : this.definitions) {
                visited = new HashSet<Node>();
                range = new Stack<UD>();
                dfsCheck(def.getNode());
            }
        }
                
        public boolean liveAt(Node node) {
            UD ud = new UD(node.getParentBlock(), node);
            return this.liveRange.contains(ud);
        }
        
        private void dfsCheck(Node node) {
            
            // Stop if definition is found (and not first node)
            UD current = new UD(node.getParentBlock(), node);
            if (this.containsDef(current) && !range.isEmpty()) {
                return;
            }
            
            // Add to visited and push onto stack
            visited.add(node);
            range.push(current);
                        
            // Search children
            for (Node child : node.getChildren()) {
                if (child!=null) {
                    if (!visited.contains(child)) {
                        dfsCheck(child);
                    } else {
                        // Loop, check if it was live
                        if (node.getLiveVars().contains(this.id)) {
                            this.liveRange.addAll(range);
                        }
                    }
                }
            } 
            
            // Add stack to live range if current is in web uses          
            if (this.containsUse(current)) {
                this.liveRange.addAll(range);
            }
            
            // Pop current node
            range.pop();
            
        }
        
        public boolean isSpilled() {
            return this.spilled;
        }
        
        public REG getRegister() {
            if (!this.isSpilled()) {
                return this.reg;
            } else {
                throw new Error("no register available");
            }
        }
        
        private boolean containsUse(UD ud) {
            return this.uses.contains(ud);
        }
        
        private boolean containsDef(UD ud) {
            return this.definitions.contains(ud);
        }
        
        public boolean containsDef(Node node) {
            UD ud = new UD(node.getParentBlock(), node);
            return containsDef(ud);
        }
        
        public boolean containsUse(Node node) {
            UD ud = new UD(node.getParentBlock(), node);
            return containsUse(ud);
        }
        
//        public boolean matchDef(String useId, Node node) {
//            UD use = new UD(node.getParentBlock(), node);
//            return this.id.getId().equals(useId) && this.containsDef(use);
//        }
        
        public boolean interfere(Web that) {
            return this._interfere(that) || that._interfere(this);
        }
               
        private boolean _interfere(Web that) {
            for (UD def : that.definitions) {
                if (this.liveRange.contains(def)) {
                    // Check if last use
                    visited = new HashSet<Node>();
                    if (this.containsUse(def) && lastUse(def)) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }
        
        private boolean lastUse(UD use) {
            
            if (!visited.isEmpty() && this.containsUse(use)) return false;
            visited.add(use.getNode());
            
            boolean check = true;
            for (Node child : use.getNode().getChildren()) {
                if (child != null) {
                    UD childUd = new UD(child.getParentBlock(), child);
                    if (!visited.contains(child)) {
                        check &= lastUse(childUd);
                    } else {
                        return check && !this.containsUse(childUd);
                    }
                }
            }
            return check;
        }
        
        public Set<REG> getBoundRegs() {
            
            Set<REG> boundRegs = new HashSet<REG>();
            
            // Bound if web for function parameter
            for (ParameterDescriptor par : RegisterAllocation.currentMethod.getPars()) {
                if (par.getId().equals(this.id.getId())) {
                    try {
                        REG parReg = getCallRegister(RegisterAllocation.currentMethod.getPars().indexOf(par));
                        boundRegs.add(parReg);
                    } catch (IllegalArgumentException err) {
                        continue;
                    }
                }
            }
                   
            // Bound if used in function call
            for (UD use : this.uses) {
                if (use.getNode().isStatement()) {
                    CfgStatement node = (CfgStatement) use.getNode();
                    IrStatement stat = node.getStatement();
                    IrExpression exp;
                    if (stat.isAssignment()) {
                        IrAssignment ass = (IrAssignment) stat;
                        exp = ass.getExpression();
                    } else if (stat.isInvokeStatement()) {
                        IrInvokeStatement inv = (IrInvokeStatement) stat;
                        exp = inv.getMethod();
                    } else {
                        throw new Error("Unexpected type");
                    }
                    if (exp.getExpKind().equals(IrExpression.expKind.CALL) || 
                            exp.getExpKind().equals(IrExpression.expKind.METH)) { 
                        IrCallExpression call = (IrCallExpression) exp;
                        for (int i = 0; i < call.getArgs().size(); i++) {
                            if (call.getArgs().get(i).contains(this.id)) {
                                try {
                                    REG parReg = getCallRegister(i);
                                    boundRegs.add(parReg);
                                } catch (IllegalArgumentException err) {
                                    continue;
                                }
                            }
                        }
                    }
                    
                }
            }
            
            // Bound with DIV/MOD operation registers
            // --> dividend to %rax
            for (UD use : this.uses) {
                if (use.getNode().isStatement()) {
                    CfgStatement node = (CfgStatement) use.getNode();
                    IrStatement stat = node.getStatement();
                    if (stat.isAssignment()) {
                        IrExpression exp = ((IrAssignment) stat).getExpression();
                        if (exp.getExpKind().equals(IrExpression.expKind.BIN)) {
                            IrBinaryExpression bin = (IrBinaryExpression) exp;
                            if (bin.getOp().equals(IrBinaryExpression.BinaryOperator.DIVIDE) || 
                                    bin.getOp().equals(IrBinaryExpression.BinaryOperator.MOD)) {
                                if (bin.getLHS().contains(this.id)) {
                                    boundRegs.add(REG.rax);
                                }
                            }
                        }
                    }
                }
            }
            
            // --> quotient to %rax, remainder to %rdx
            for (UD def : this.definitions) {
                if (!def.getNode().isStatement()) {
                    continue;
                }
                CfgStatement node = (CfgStatement) def.getNode();
                IrExpression exp = ((IrAssignment) node.getStatement()).getExpression();
                if (exp.getExpKind().equals(IrExpression.expKind.BIN)) {
                    IrBinaryExpression bin = (IrBinaryExpression) exp;
                    if (bin.getOp().equals(IrBinaryExpression.BinaryOperator.DIVIDE)) {
                        boundRegs.add(REG.rax);
                    } else if (bin.getOp().equals(IrBinaryExpression.BinaryOperator.MOD)) {
                        boundRegs.add(REG.rdx);
                    }
                }
            }
            
            // Bound with returned value
            for (UD def : this.definitions) {
                if (def.getNode().isStatement()) {
                    CfgStatement node = (CfgStatement) def.getNode();
                    IrStatement stat = node.getStatement();
                    if (stat.isAssignment()) {
                        IrExpression exp = ((IrAssignment) stat).getExpression();
                        if (exp.getExpKind().equals(IrExpression.expKind.METH) || 
                                exp.getExpKind().equals(IrExpression.expKind.CALL)) {
                            boundRegs.add(REG.rax);
                        }
                    }
                }
            }
            
            // Bound with return value
            for (UD use : this.uses) {
                Node node = use.getNode();
                if (!node.hasNext()) {
                    boundRegs.add(REG.rax);
                }
            }
            
            return boundRegs;
        }
        
        @Override
        public String toString() {
            String str = "\nWeb" + this.symReg + " (" + this.id.toString() + ")";
            if (spilled) {
                if (offset != 0) {
                    str += "\tmemory = " + offset;
                }
            } else {
                if (reg != null) {
                    str += "\tregister = " + reg.toString();
                }
            }
            for (UD def : definitions) {
                str += "\n\t" + def.toString();
            }
            return str;
        }
        
        public int getSpillCost() {
            int cost = 0;
            
            for (UD def : this.definitions) {
                cost += Math.pow(10, def.getNode().getDepth());
            }
            for (UD use : this.uses) {
                cost += Math.pow(10, use.getNode().getDepth());
            }
            
            return cost;
        }

        public IrIdentifier getId() {
            return this.id;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
        
    }

    private static class InterferenceGraph {
        
        private int nSym;
        private int nPhys;
        
        private boolean[][] adjMat;
        private Map<Integer, NodeRecord> adjList;
        private List<Web> webs;
        
        private Map<REG, Set<Web>> boundRegs;
        private Map<Web, Set<REG>> boundWebs;
        
        private Set<Web> boundStack = new HashSet<Web>();
        
        public void color() {
            Stack<NodeRecord> stack = new Stack<NodeRecord>();
            
            // Stack nodes
            while (!adjList.isEmpty()) {
                
                // Push R-colorable onto stack
                boolean loop = true;
                while(loop) {
                    loop = false;
                    for (NodeRecord nr : adjList.values()) {
                        if (nr.getDegree() < nPhys) {
                            stack.push(nr);
                            adjList.remove(nr.getIndex());
                            removeFromAdjoints(nr.getIndex());
                            loop = true;
                            break;
                        }
                    }
                }
                
                // Spill register with lowest cost
                if (!adjList.isEmpty()) {
                    List<NodeRecord> popList = new ArrayList<NodeRecord>(adjList.values());
                    Collections.sort(popList);
                    NodeRecord sp = popList.get(0);
                    stack.push(sp);
                    adjList.remove(sp.getIndex());
                    removeFromAdjoints(sp.getIndex());
                }
            }
            
            // Color graph
            while (!stack.isEmpty()) {
                NodeRecord nr = stack.pop();
                nr.restoreAdjoint();
                adjList.put(nr.getIndex(), nr);
                
                nr.setColor(getColor(nr.getIndex()));
                
                Iterator<NodeRecord> it = stack.iterator();
                while (it.hasNext()) {
                    NodeRecord st = it.next();
                    st.restoreAdjoint(nr.getIndex());
                }
            }
        }
        
        public REG getRegister(Web web) {
            int webColor = adjList.get(getInd(web)).getColor();
            for (int i = 0; i < nPhys; i++) {
                int regColor = adjList.get(i).getColor();
                if (webColor == regColor) {
                    return getRegAtInd(i);
                }
            }
            throw new Error("Cannot find register");
        }

        public boolean canAllocate(Web web) {
            return !adjList.get(getInd(web)).isSpilled();
        }

        public int getColor(int n) {
            List<Integer> colors = new ArrayList<Integer>();
            for (int i = 0; i < nPhys; i++) {
                colors.add(i);
            }
            
            for (int i : adjList.get(n).getAdjoints()) {
                if (adjList.containsKey(i)) {
                    colors.remove((Integer)adjList.get(i).getColor());
                }
            }
            
            return colors.isEmpty() ? -1 : colors.get(0);
        }
        
        private void removeFromAdjoints(int ind) {
            for (NodeRecord nr : this.adjList.values()) {
                nr.removeInd(ind);
            }
        }
                
        public InterferenceGraph(Set<Web> webs) {
            
            boundRegs = new HashMap<REG, Set<Web>>();
            boundWebs = new HashMap<Web, Set<REG>>();
            
            // Initialize
            this.nSym = webs.size();
            this.nPhys = REG.values().length;
            this.webs = new ArrayList<Web>(webs);
            this.adjMat = new boolean[nSym+nPhys][nSym+nPhys];
            this.adjList = new HashMap<Integer, NodeRecord>();
            
            // Reg2Reg interference     --> all physical registers interfere with one another
            for (int i = 0; i < this.nPhys; i++) {
                for (int j = 0; j <= i; j++) {
                    adjMat[i][j] = true;
                }
            }
            
            // Sym2Reg interference     --> bound webs to registers if possible
            for (Web web : this.webs) {
                Set<REG> boundRegs = web.getBoundRegs(); 
                //System.out.println("Web " + web.id + " <-- " + boundRegs + "\n");
                boundWebs.put(web, boundRegs);
                attemptBound(web, boundRegs);
            }
            
            // Sys2Sym interference     --> check if webs interfere
            for (Web web1 : this.webs) {
                for (Web web2 : this.webs) {
                    if (web1.equals(web2) || web1.interfere(web2)) {
                        adjMat[getInd(web1)][getInd(web2)] = true;
                    }
                }
            }
            
            // Build adjacency list
            makeAdjList();
        }
        
        private void attemptBound(Web web, Set<REG> regs) {
            
            boundStack.add(web);
            
            // First try all possible for current
            Iterator<REG> it = regs.iterator();  
            while(it.hasNext()) {
                REG reg = it.next();
                boolean skip = false;
                
                // Check if live together with conflicting webs
                if (isBound(reg)) {
                    Set<Web> conflictWebs = this.boundRegs.get(reg);
                    for (Web confWeb : conflictWebs) {
                        if (web.interfere(confWeb)) {
                            skip = true;
                        }
                    }
                }

                // Bind to web
                if (!skip) {
                    bind(web, reg);
                    this.boundWebs.get(web).remove(reg);
                    boundStack.remove(web);
                    return;
                }

            }
            
            // Then relocate conflict
            it = regs.iterator();  
            while(it.hasNext()) {
                REG reg = it.next();
                Set<Web> conflict = this.boundRegs.get(reg);
                for (Web confWeb : conflict) {
                    if (!boundStack.contains(confWeb)) {
                        attemptBound(confWeb, this.boundWebs.get(confWeb));
                    }
                }
                bind(web, reg);
                this.boundWebs.get(web).remove(reg);
                boundStack.remove(web);
                return;
            }
            
            // Unbound if no binding possible
            for (int i = 0; i < nPhys; i++) {
                adjMat[getInd(web)][i] = false;
            }
            
            boundStack.remove(web);
            
        }
        
        private boolean isBound(REG reg) {
            return boundRegs.containsKey(reg);
        }
        
        private void bind(Web web, REG reg) {
            // Update bounded registers record
            if (boundRegs.containsKey(reg)) {
                boundRegs.get(reg).add(web);
            } else {
                Set<Web> webSet = new HashSet<Web>();
                webSet.add(web);
                boundRegs.put(reg, webSet);
            }
            
            // Update adjacency matrix
//            adjMat[getInd(web)][getInd(reg)] = false;
            for (int i = 0; i < nPhys; i++) {
                if (!reg.equals(REG.values()[i])) {
                    adjMat[getInd(web)][i] = true;
                } else {
                    adjMat[getInd(web)][i] = false;
                }
            }
//            for (Web otherWeb : this.webs) {
//                if (!web.equals(otherWeb)) {
//                    adjMat[getInd(otherWeb)][getInd(reg)] = true;
//                }
//            }
        }
        
        private int getInd(Web web) {
            return webs.indexOf(web) + this.nPhys;
        }
        
//        private int getInd(REG reg) {
//            for (int i = 0; i < nPhys; i++) {
//                if (REG.values()[i].equals(reg)){
//                    return i;
//                }
//            }
//            throw new Error("Cannot found register");
//        }
               
        private boolean interfere(int i, int j) {
            if (i < j) {
                return interfere(j, i);
            } else {
                return adjMat[i][j];
            }
        }
        
        public void makeAdjList() {
            for (int i = 0; i < this.nPhys; i++) {
                adjList.put(i, new NodeRecord(i));
                adjList.get(i).setAdjoints(getAdjoints(i));
            }
            
            for (Web web : webs) {
                adjList.put(getInd(web), new NodeRecord(getInd(web)));
                adjList.get(getInd(web)).setAdjoints(getAdjoints(getInd(web)));
            }
        }
        
        private List<Integer> getAdjoints(int i) {
            List<Integer> adjList = new ArrayList<Integer>();
            for (int j = 0; j < nPhys+nSym; j++) {
                if (i == j) continue;
                if (interfere(i, j)) {
                    adjList.add(j);
                }
            }
            return adjList;
        }
        
        private REG getRegAtInd(int i) {
            return REG.values()[i];
        }
        
        private Web getWebAtInd(int i) {
            return webs.get(i-nPhys);
        }
        
        public void computeSpillCost() {
            for (int i = nPhys; i < nPhys+nSym; i++) {
                this.adjList.get(i).setSpillCost(getWebAtInd(i).getSpillCost());
            }
        }
        
        @Override
        public String toString() {

            // Adjacency list
            String str = "Interference Graph:";
            for (Integer i : adjList.keySet()) {
                str += "\n " + i + "\t";
                if (i < nPhys) {
                    str += String.format("%-20s", getRegAtInd(i).toString());
                } else {
                    str += String.format("%-20s", "Web " + getWebAtInd(i).symReg + " (" + getWebAtInd(i).id + ")");
                }
                str += "\t" + adjList.get(i).toString();
            }
            
            // Adjacency matrix
            str += "\n\n";
            for (int i = 0; i < nPhys+nSym; i++) {
                for (int j = 0; j < nPhys+nSym; j++) {
                    str += " " + (adjMat[i][j] ? "1" : "0");
                }
                str += "\n";
            }

            return str + "\n\n";
        }
        
        private class NodeRecord implements Comparable<NodeRecord>{
            
            private int index;
            private int color;
            private boolean spilled;
            private int spillCost;
            private List<Integer> adjoints;
            private List<Integer> removedAdjoints;
            
            public NodeRecord(int ind) {
                this.index = ind;
                this.color = -1;
                this.spilled = false;
                this.spillCost = -1;
                this.adjoints = new ArrayList<Integer>();
                this.removedAdjoints = new ArrayList<Integer>();
            }
            
            public boolean isSpilled() {
                return this.spilled;
            }

            public int getIndex() {
                return this.index;
            }
            
            public int getDegree() {
                return adjoints.size();
            }
            
            public void setAdjoints(List<Integer> adj) {
                this.adjoints.addAll(adj);
            }
            
            public void setSpillCost(int c) {
                this.spillCost = c;
            }
            
            public void setColor(int c) {
                this.color = c;
                if (c == -1) {
                    this.spilled = true;
                }
            }
            
            public int getColor() {
                return this.color;
            }
            
            public void removeInd(int ind) {
                adjoints.remove((Integer)ind);
                removedAdjoints.add(ind);
            }
            
            public List<Integer> getAdjoints() {
                return adjoints;
            }
            
            public void restoreAdjoint() {
                adjoints.addAll(removedAdjoints);
                removedAdjoints.clear();
            }
            
            public void restoreAdjoint(int ind) {
                if (this.removedAdjoints.contains(ind)) {
                    this.removedAdjoints.remove((Integer)ind);
                    this.adjoints.add(ind);
                }
            }
            
            @Override
            public String toString() {
                String str = String.format("%d: color %d, spilled %d, cost %d", index, color, spilled ? 1 : 0, spillCost);
                str += " adjacent = " + adjoints;
                return str;
            }

            @Override
            public int compareTo(NodeRecord that) {
                if (this.spillCost == -1 && that.spillCost == -1) {
                    return 0;
                } else if (this.spillCost == -1) {
                    return 1;
                } else if (that.spillCost == -1) {
                    return -1;
                } else {
                    return this.spillCost - that.spillCost;
                }
            }
        }
    }
    
    public static enum REG {
        rax,        // (return value)
        rbx,        
        rcx,        // (arg 4)
        rdx,        // (arg 3)
        //rsp,      // Stack pointer
        //rbp,      // Base pointer
        rsi,        // (arg 2)
        rdi,        // (arg 1)
        r8,         // (arg 5)
        r9,         // (arg 6)
        //r10,      // Scrap register
        //r11,      // Scrap register (move to array if both src and index in main memory e.g. movq a(,%r11,8), %r10)
        r12,
        r13,
        r14,
        r15
    }
    
    public static REG getCallRegister(int i) throws IllegalArgumentException {
        switch (i) {
        case 0:
            return REG.rdi;
        case 1:
            return REG.rsi;
        case 2:
            return REG.rdx;
        case 3:
            return REG.rcx;
        case 4:
            return REG.r8;
        case 5:
            return REG.r9;
        default:
            throw new IllegalArgumentException("No register assigned to this value");
        }
    }
}