package cfg.Optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;

/**
 * @author Nicola
 */
public class RegisterAllocation {

    public void allocate(MethodCFG cfg) {
                
        // Get DU chains from reaching definitions dataflow analysis
        Set<DuChain> duChains = new ReachingDefinitions(cfg).getDuChains();
        
        // Merge DU chains in Webs for register allocation  
        Set<Web> webs = Web.getWebs(duChains);
        
        // TODO build dependency graph
        
        // TODO coalesce registers
        
        // TODO build adjacency list
        
        // TODO compute spill costs
        
        // TODO color graph
        
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
            
            public String toString() {
                return defSet.toString();
            }
            
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
            
            public int hashCode() {
                return id.hashCode();
            }
            
            public boolean equals(Object thatObj) {
                if (!(thatObj instanceof Definition)) {
                    return false;
                }
                Definition that = (Definition) thatObj;
                return (this.id.equals(that.id)) && (this.node.equals(that.node));
            }
            
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
    
    private class UD {
        private CfgBlock block;
        private Node node;
        
        public UD (CfgBlock block, Node node) {
            this.block = block;
            this.node = node;
        }
        
        public CfgBlock getBlock() {
            return this.block;
        }
        
        public Node getNode() {
            return this.node;
        }
        
        public int hashCode() {
            return 0;
        }
        
        public boolean equals(Object thatObj) {
            if (!(thatObj instanceof UD)) {
                return false;
            }
            UD that = (UD) thatObj;
            return block.equals(that.block) && node.equals(that.node);
        }
        
        public String toString() {
            return block.getBlockName() + ": " + node.toString();
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
        
        public int hashCode() {
            return id.hashCode();
        }
        
        public boolean equals(Object thatObj) {
            if (!(thatObj instanceof DuChain)) {
                return false;
            }
            DuChain that = (DuChain) thatObj;
            return id.equals(that.id) && definition.equals(that.definition);
        }
        
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
    
    private static class Web {
                
        private IrIdentifier id;
        private Set<UD> definitions;
        private Set<UD> uses;
        private boolean spilled;
        private int symReg;
        
        private static int webNum = 0;
        private static Set<Web> webs;
        
        public Web(IrIdentifier id, UD def, Set<UD> uses) {
            this.id = id;
            definitions = new HashSet<UD>();
            this.definitions.add(def);
            this.uses = uses;
            this.spilled = false;
            this.symReg = ++webNum;
        }
        
        public static Set<Web> getWebs(Set<DuChain> chains) {
            
            webs = new HashSet<Web>();
            Stack<DuChain> stack = new Stack<DuChain>();
            stack.addAll(chains);
            
            while(!stack.isEmpty()) {
                DuChain chain = stack.pop();
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
        
    }
}