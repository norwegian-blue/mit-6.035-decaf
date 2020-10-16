package semantic;

/**
 * @author nicola
 */
public abstract class TypeDescriptor {
    
    protected final String typeName;
    
    public TypeDescriptor(String typeName) {
        this.typeName = typeName;
    }
    
    @Override
    public String toString() {
        return typeName;
    }

}
