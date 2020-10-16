package semantic;

/**
 * @author Nicola
 */
public abstract class Descriptor {
    
    protected String name;
    
    public Descriptor(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

}
