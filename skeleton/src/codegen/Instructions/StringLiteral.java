package codegen.Instructions;

/**
 * @author Nicola
 */
public class StringLiteral extends Exp {

    private String stringContent;
    private String stringId;
    
    public StringLiteral(String stringContent, String stringId) {
        this.stringContent = stringContent;
        this.stringId = stringId;
    }
        
    @Override
    public String toCode() {
        return "$." + this.stringId;
    }
    
    public String toAllocation() {
        String str;
        str = "." + stringId + ":\n";
        str += "\t.string \"" + stringContent + "\"";
        return str;
    }
    
    @Override
    public boolean equals(Object thatObj) {
        if (!(thatObj instanceof StringLiteral)) {
            return false;
        }
        StringLiteral that = (StringLiteral) thatObj;
        return that.stringId.equals(this.stringId);
    }
    
    @Override
    public int hashCode() {
        return this.stringContent.hashCode();
    }
    
    @Override
    public boolean isString() {
        return true;
    }

    public String getId() {
        return stringId;
    }
    
    public String getValue() {
        return stringContent;
    }
    
}
