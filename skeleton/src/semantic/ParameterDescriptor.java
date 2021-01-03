package semantic;

import codegen.Instructions.Location;
import ir.Expression.IrIdentifier;

/**
 * @author Nicola
 */
public class ParameterDescriptor extends Descriptor {
    
    private Location location;
    private Location destination;
    
    public ParameterDescriptor(String name, TypeDescriptor type) {
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
    
    public IrIdentifier getIrId() {
        return new IrIdentifier(this.getId());
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
    
    @Override
    public String toString() {
        return "[PAR] " + this.type.toString() + " " + this.name;
    }
       
}
