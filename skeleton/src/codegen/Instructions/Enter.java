package codegen.Instructions;

public class Enter extends LIR {

    private int localNum;
    
    public Enter(int localNum) {
        this.localNum = localNum;
    }
    
    @Override
    public String toCode() {
        return "\tenter\t$" + localNum + ", $0";
    }
}
