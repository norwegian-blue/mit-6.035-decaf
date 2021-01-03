package semantic;

import codegen.Instructions.Location;
import ir.Expression.IrIdentifier;

/**
 * @author Nicola
 */
public class LocalDescriptor extends Descriptor {
    
    private Location location;
    private Location destination;
    
    public LocalDescriptor(String name, TypeDescriptor type) {
        super(name, type);
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Location getLocation() {
        if (this.location == null) {
            throw new Error("Cannot find location");
        }
        return this.location;
    }
    
    public void setDestination(Location destination) {
        this.destination = destination;
    }
    
    public Location getDestination() {
        if (this.destination == null) {
            throw new Error("Cannot find destination");
        }
        return this.destination;
    }
    
    public IrIdentifier getIrId() {
        return new IrIdentifier(this.getId());
    }
    
    @Override
    public String toString() {
        return "[LOCAL] " + this.type.toString() + " " + this.name;
    }   
}
