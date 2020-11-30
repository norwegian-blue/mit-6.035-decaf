package semantic;

/**
 * @author Nicola
 */
public abstract class Descriptor {
    
    protected String name;
    protected TypeDescriptor type;
    private String alias;
    private int offset;
    
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
    
    public String getId() {
        return name;
    }
    
    public int getSize() {
        return this.type.getSize();
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public boolean isGlobal() {
        return false;
    }

}
