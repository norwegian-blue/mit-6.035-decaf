package codegen.Instructions;

/** 
 * @author Nicola
 */

public class Memory extends Location {

    private int offset;
    
    public Memory(int offset) {
        this.offset = offset;
    }
    
    @Override
    public String toString() {
        String sign = (offset>0) ? "+" : "";
        return "BP" + sign + offset;
    }
    
    @Override
    public String toCode() {
        return offset + "(%rbp)";
    }
    
    @Override
    public boolean equals(Object thatObj) {
        if (!(thatObj instanceof Memory)) {
            return false;
        }
        Memory that = (Memory) thatObj;
        return this.offset == that.offset;
    }
    
}
