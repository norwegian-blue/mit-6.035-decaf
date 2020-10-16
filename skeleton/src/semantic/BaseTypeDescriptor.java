package semantic;

/**
  * @author Nicola
*/
public class BaseTypeDescriptor extends TypeDescriptor {
    
    protected BaseTypeDescriptor(String typeName) {
        super(typeName);
    }
    
    public static BaseTypeDescriptor INT = new BaseTypeDescriptor("int");
    public static BaseTypeDescriptor BOOL = new BaseTypeDescriptor("bool");
    public static BaseTypeDescriptor VOID = new BaseTypeDescriptor("void");
    public static BaseTypeDescriptor STRING = new BaseTypeDescriptor("string");
 
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof BaseTypeDescriptor)) return false;
        BaseTypeDescriptor thatType = (BaseTypeDescriptor) thatObject;
        return thatType.typeName == this.typeName;
    }
}