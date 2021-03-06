package semantic;

/**
  * @author Nicola
*/
public class BaseTypeDescriptor extends TypeDescriptor {
    
    protected BaseTypeDescriptor(String typeName) {
        super(typeName);
    }
    
    public static BaseTypeDescriptor INT = new BaseTypeDescriptor("INT");
    public static BaseTypeDescriptor BOOL = new BaseTypeDescriptor("BOOL");
    public static BaseTypeDescriptor VOID = new BaseTypeDescriptor("VOID");
    public static BaseTypeDescriptor STRING = new BaseTypeDescriptor("STRING");
    public static BaseTypeDescriptor undefined = new BaseTypeDescriptor("undefined");
 
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof BaseTypeDescriptor)) return false;
        BaseTypeDescriptor thatType = (BaseTypeDescriptor) thatObject;
        if (this.typeName.equals("undefined") || thatType.typeName.equals("undefined")) {
            return true;
        } else {
            return thatType.typeName.equals(this.typeName);
        }
    }
}