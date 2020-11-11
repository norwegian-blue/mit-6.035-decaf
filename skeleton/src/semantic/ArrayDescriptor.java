package semantic;

public class ArrayDescriptor extends TypeDescriptor {
    
    private final BaseTypeDescriptor elementType;
    private final int length;
    
    public ArrayDescriptor(BaseTypeDescriptor elementType, int length) {
        super("array");
        this.elementType = elementType;
        this.length = length;
    }
    
    public TypeDescriptor getBaseType() {
        return this.elementType;
    }
    
    @Override
    public String toString() {
        return elementType.toString() + "[" + length + "]";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if ((thatObject instanceof BaseTypeDescriptor) && thatObject.equals(BaseTypeDescriptor.undefined)) return true;
        if (!(thatObject instanceof ArrayDescriptor)) return false;
        if (this.getBaseType().equals(BaseTypeDescriptor.undefined)) return true;
        ArrayDescriptor thatArray = (ArrayDescriptor) thatObject;
        return thatArray.elementType.equals(this.elementType);
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public int getLength() {
        return length;
    }
}
