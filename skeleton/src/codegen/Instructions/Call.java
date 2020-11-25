package codegen.Instructions;

public class Call extends LIR {

    private String method;
    
    public Call(String method) {
        this.method = method;
    }

    @Override
    public String toCode() {
        return "call\t" + method;
    }
    
}
