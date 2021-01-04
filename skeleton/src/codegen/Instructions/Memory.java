package codegen.Instructions;

/** 
 * @author Nicola
 */

public class Memory extends Location {

    private int offset;
    
    public Memory(int offset, int size) {
        this.offset = offset;
        this.size = size;
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
