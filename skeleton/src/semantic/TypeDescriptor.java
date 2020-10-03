package semantic;

/**
  * @author Nicola
*/
public class TypeDescriptor {
    
    private final String typeName;
    
    protected TypeDescriptor(String typeName) {
        this.typeName = typeName;
    }
    
    public static TypeDescriptor INT = new TypeDescriptor("int");
    public static TypeDescriptor BOOL = new TypeDescriptor("bool");
    public static TypeDescriptor VOID = new TypeDescriptor("void");
    public static TypeDescriptor STRING = new TypeDescriptor("string");
    
    public static TypeDescriptor array(TypeDescriptor elementType) {
        return new ArrayDescriptor(elementType);
    }
    
    @Override
    public String toString() {
        return typeName;
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof TypeDescriptor)) return false;
        TypeDescriptor thatType = (TypeDescriptor) thatObject;
        return thatType.typeName == this.typeName;
    }
}