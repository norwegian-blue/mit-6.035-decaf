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