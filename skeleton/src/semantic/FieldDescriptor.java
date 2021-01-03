package semantic;

import codegen.Instructions.Global;
import codegen.Instructions.Location;
import ir.Expression.IrIdentifier;

/**
 * @author Nicola
 */
public class FieldDescriptor extends Descriptor {
    
    private Location location;
    
    public FieldDescriptor(String name, TypeDescriptor type) {
        super(name, type);
        this.location = new Global(name);
    }
    
    @Override
    public String toString() {
        return "[FIELD] " + this.type.toString() + " " + this.name; 
    }  
    
    @Override
    public boolean isGlobal() {
        return true;
    }
    
    public IrIdentifier getIrId() {
        return new IrIdentifier(this.getId());
    }
    
    public Location getLocation() {
        return this.location;
    }
}
