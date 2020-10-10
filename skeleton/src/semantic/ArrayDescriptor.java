package semantic;

public class ArrayDescriptor extends TypeDescriptor {
    
    private final TypeDescriptor elementType;
    private final int length;
    
    public ArrayDescriptor(TypeDescriptor elementType, int length) {
        super("array");
        this.elementType = elementType;
        this.length = length;
    }
    
    @Override
    public String toString() {
        return elementType.toString() + "[" + length + "]";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof ArrayDescriptor)) return false;
        ArrayDescriptor thatArray = (ArrayDescriptor) thatObject;
        return thatArray.elementType.equals(this.elementType);
    }
}
