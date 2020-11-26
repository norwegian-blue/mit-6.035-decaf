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
    
    @Override
    public abstract boolean equals(Object thatObject);

    public boolean isArray() {
        return false;
    }
    
    public int getLength() {
        return 1;
    }
    
    // Defaults to 64 bit
    public int getSize() {
        return 8;
    }
}
