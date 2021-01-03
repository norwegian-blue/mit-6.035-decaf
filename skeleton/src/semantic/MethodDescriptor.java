package semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cfg.Nodes.Node;
import cfg.Optimization.RegisterAllocation.Web;
import codegen.Instructions.Location;
import codegen.Instructions.Memory;
import codegen.Instructions.Register;
import ir.Expression.IrIdentifier;

/**
 * @author Nicola
 */
public class MethodDescriptor extends Descriptor {

    private List<ParameterDescriptor> parameters;
    private List<LocalDescriptor> locals;
    private List<FieldDescriptor> globals;
    
    private Node currentNode;    
    private Set<Web> webs;
    
    private int stackTop = 8;
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters) {
        super(name, returnType);
        this.parameters = parameters;
        this.locals = new ArrayList<LocalDescriptor>();
        this.globals = new ArrayList<FieldDescriptor>();
    }
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters, List<LocalDescriptor> locals) {
        super(name, returnType);
        this.parameters = parameters;
        this.locals = locals;
        this.globals = new ArrayList<FieldDescriptor>();
    }
    
    public void addWebs(Set<Web> webs) {
        this.webs = webs;
    }
    
    public Set<Web> getWebs() {
        return this.webs;
    }
    
    @Override
    public boolean isMethod() {
        return true;
    }
    
    public List<ParameterDescriptor> getPars() {
        return this.parameters;
    }
    
    @Override
    public String toString() {
        String str = "[METHOD] " + this.type.toString() + " (";
        for (ParameterDescriptor par : parameters) {
            str += par.toString() + ", ";
        }
        if (!parameters.isEmpty()) {
            str = str.substring(0, str.length()-2);
        }
        return str + ")";
    }  
    
    public List<LocalDescriptor> getLocals() {
        return locals;
    }
    
    public void addLocal(LocalDescriptor local) {
        this.locals.add(local);
    }
    
    public void addGlobal(FieldDescriptor global) {
        this.globals.add(global);
    }
    
    public void setCurrentNode(Node node) {
        this.currentNode = node;
    }
    
    public Location getLocation(IrIdentifier id) {
        for (ParameterDescriptor par : parameters) {
            if (par.getIrId().equals(id)) {
                return par.getLocation();
            }
        }
        for (LocalDescriptor loc : locals) {
            if (loc.getIrId().equals(id)) {
                return loc.getLocation();
            }
        }
        throw new Error("cannot find identifier location");
    }
    
    public void setLocation(IrIdentifier id, Location location) {
        for (ParameterDescriptor par : parameters) {
            if (par.getIrId().equals(id)) {
                par.setLocation(location);
                return;
            }
        }
        for (LocalDescriptor loc : locals) {
            if (loc.getIrId().equals(id)) {
                loc.setLocation(location);
                return;
            }
        }
        throw new Error("cannot find identifier");
    }
    
    public Location getDestination(IrIdentifier id) {
        for (ParameterDescriptor par : parameters) {
            if (par.getIrId().equals(id)) {
                return par.getDestination();
            }
        }
        for (LocalDescriptor loc : locals) {
            if (loc.getIrId().equals(id)) {
                return loc.getDestination();
            }
        }
        throw new Error("cannot find identifier destination");
    }
    
    public void setDestination(IrIdentifier id, Location location) {
        for (ParameterDescriptor par : parameters) {
            if (par.getIrId().equals(id)) {
                par.setDestination(location);
                return;
            }
        }
        for (LocalDescriptor loc : locals) {
            if (loc.getIrId().equals(id)) {
                loc.setDestination(location);
                return;
            }
        }
        throw new Error("cannot find identifier");
    }
    
    
    
    public void setStack() {
        
        // If no register allocation, put everything on stack
        if (webs == null) {
            for (ParameterDescriptor par : parameters) {
                par.setLocation(new Memory(-stackTop));
                par.setDestination(new Memory(-stackTop));
                stackTop += par.getSize();
            }
            for (LocalDescriptor loc : locals) {
                loc.setLocation(new Memory(-stackTop));
                loc.setDestination(new Memory(-stackTop));
                stackTop += loc.getSize();
            }
            return;
        }
        
        // Assign new stack space only to spilled webs
        for (Web web : webs) {
            if (web.isSpilled()) {
                IrIdentifier id = web.getId();
                
                boolean found = false;
                
                // Push parameter on stack only if not already there by call convention
                for (ParameterDescriptor par : parameters) {
                    if (par.getIrId().equals(id)) {
                        int i = parameters.indexOf(par);
                        if (i < 6) {
                            web.setOffset(-stackTop);               // Push parameter on stack                      
                            stackTop += par.getSize();
                        } else {
                            web.setOffset(8+(i-5)*par.getSize());   // Parameter already on stack (call convention)
                        }
                        found = true;
                    }
                }
                
                for (LocalDescriptor loc : locals) {
                    if (loc.getIrId().equals(id)) {
                        web.setOffset(stackTop);                    // Push local on stack                      
                        stackTop += loc.getSize();
                    }
                }
                
                if (!found) throw new Error("Cannot find web identifier");
            }
        }    
    }
    
    public boolean isLive(IrIdentifier id) {
        // Always live if register allocation not performed
        if (webs == null) return true;
        
        for (IrIdentifier live : currentNode.getLiveVars()) {
            if (live.equals(id)) {
                return true;
            }
        }
        return false;
    }
    
    public int getStackTop() {
        return stackTop;
    }
    
    public void updateLocations(Node node) {
        if (webs == null) return;
        
        // Update variables location based on live webs
        for (Web web : webs) {
            if (web.liveAt(node)) {
                
                // Get location
                Location location;
                if (web.isSpilled()) {
                    location = new Memory(web.getOffset());
                } else {
                    location = new Register(web.getRegister());
                }
                
                // Update destination if definition
                if (web.containsDef(node)) {
                    this.setDestination(web.getId(), location);
                } else {
                    this.setLocation(web.getId(), location);
                }
            }
        }
    }
    
    public List<Register> getUsedRegs() {
        List<Register> regs = new ArrayList<Register>();
        if (webs == null) return regs;
        
        for (Web web : webs) {
            if (!web.isSpilled()) {
                regs.add(new Register(web.getRegister()));
            }
        }
        return regs;        
    }
}
