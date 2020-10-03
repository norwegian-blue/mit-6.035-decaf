package semantic;

public class ArrayDescriptor extends TypeDescriptor {
    
    private final TypeDescriptor elementType;
    
    public ArrayDescriptor(TypeDescriptor elementType) {
        super("array");
        this.elementType = elementType;
    }
    
    @Override
    public String toString() {
        return elementType.toString() + "[]";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof ArrayDescriptor)) return false;
        ArrayDescriptor thatArray = (ArrayDescriptor) thatObject;
        return thatArray.elementType.equals(this.elementType);
    }
}
