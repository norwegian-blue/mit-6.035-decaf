package cfg.Optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cfg.MethodCFG;
import cfg.Nodes.*;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;

/**
 * @author Nicola
 */
public class RegisterAllocation {

    public void allocate(MethodCFG cfg) {
        
        // Get UD chains from reaching definitions dataflow analysis
        ReachingDefinitions reachDef = new ReachingDefinitions(cfg);
        Set<UdChain> udChains = reachDef.getUdChains();
        
        // TODO get Webs
        
        // TODO build dependency graph
        
        // TODO coalesce registers
        
        // TODO build adjacency list
        
        // TODO compute spill costs
        
        // TODO color graph
        
    }
    
    private class ReachingDefinitions {
        
        private Map<CfgBlock, Set<Definition>> RCHin;
        private Map<CfgBlock, Set<Definition>> RCHout;
        private List<CfgBlock> blocks;
        
        public Set<UdChain> getUdChains() {
            // TODO implement UdChains finder
            return null;
        }
        
        public ReachingDefinitions(MethodCFG cfg) {
            // Get method blocks
            blocks = new ArrayList<CfgBlock>();
            for (Node block : cfg.getNodes()) {
                blocks.add(block.getParentBlock());
            }
            
            // Initialize maps
            RCHin = new HashMap<CfgBlock, Set<Definition>>();
            RCHout = new HashMap<CfgBlock, Set<Definition>>();
            
            // Do dataflow analysis
            this.getReachingDefinitions();
        }
        
        private void getReachingDefinitions() {
            // Initialize RCHin/out
            for (CfgBlock block : blocks) {
                RCHin.put(block, new HashSet<Definition>());
                RCHout.put(block, new HashSet<Definition>());
            }
                        
            // Iteratively solve dataflow problem
            Set<CfgBlock> changed = new HashSet<CfgBlock>(blocks);
            DefinitionsUpdate defUpd = new DefinitionsUpdate();
            
            while (!changed.isEmpty()) {
                CfgBlock currentBlock = changed.iterator().next();
                changed.remove(currentBlock);
                
                // Update RCHin_i --> union of parent RCH
                Set<Definition> RCHin_i = RCHin.get(currentBlock);
                for (Node parent : currentBlock.getParents()) {
                    RCHin_i.addAll(RCHout.get(parent.getParentBlock()));
                }
                
                // Update RCHout_i --> GEN_i + (RCHin_i - KILL_i)
                defUpd.process(currentBlock, RCHin_i);
                Set<Definition> RCHout_i = defUpd.getRCHout();
                
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

        private class Definition {
            private IrIdentifier id;
            private Node node;
            
            public Definition(IrIdentifier id, Node node) {
                this.id = id;
                this.node = node;
            }
            
            public int hashCode() {
                return id.hashCode();
            }
            
            public boolean equals(Object that) {
                if (!(that instanceof Definition)) {
                    return false;
                }
                Definition thatDefinition = (Definition) that;
                return (this.id.equals(thatDefinition.id));
            }
            
            public String toString() {
                return "{" + id.toString() + ", " + node.toString() + "}";
            }
        }
        
        private class DefinitionsUpdate implements NodeVisitor<Void> {
            
            private Set<Definition> RCHout;
            
            public void process(CfgBlock block, Set<Definition> RCHin) {
                RCHout = new HashSet<Definition>(RCHin);
                block.accept(this);
            }
            
            public Set<Definition> getRCHout() {
                return this.RCHout;
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
                // Skip non-assignment
                if (!node.getStatement().isAssignment()) {
                    return null;
                }
                
                IrAssignment ass = (IrAssignment) node.getStatement();
                IrIdentifier id = (IrIdentifier) ass.getLocation();
                
                // Skip globals
                if (id.getId().startsWith("_glb")) {
                    return null;
                }
                
                Definition newDef = new Definition(id, node);
                
                // Kill old definition if present
                if (RCHout.contains(newDef)) {
                    RCHout.remove(newDef);
                }
                RCHout.add(newDef);   // Put new definition            
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
    }
    
    private class UdChain {
        private IrIdentifier id;
        private UD definition;
        private Set<UD> uses;
        
        public UdChain(IrIdentifier id, UD definition) {
            this.id = id;
            this.definition = definition;
            this.uses = new HashSet<UD>();
        }
        
        public void addUse(UD use) {
            this.uses.add(use);
        }
        
        public void addUses(Collection<UD> uses) {
            this.uses.addAll(uses);
        }
    }
    
    
}