package semantic;

/**
 * @author Nicola
 */
public abstract class Descriptor {
    
    protected String name;
    protected TypeDescriptor type;
    private String alias;
    
    public Descriptor(String name, TypeDescriptor type) {
        this.name = name;
        this.type = type;
    }
    
    public TypeDescriptor getType() {
        return this.type;
    }
    
    public boolean isMethod() {
        return false;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }

}
