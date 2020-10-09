package ir.Statement;

import java.util.List;

/**
 * @author Nicola
 */
public class IrBlock extends IrStatement {
    private final List<IrStatement> statements;
    private final boolean isempty;
    
    public IrBlock(List<IrStatement> statements) {
        this.statements = statements;
        this.isempty = false;
    }
    
    public IrBlock() {
        this.isempty = true;
        this.statements = null;
    }
    
    public boolean isEmpty() {
        return isempty;
    }
    
    @Override 
    public String toString() {
        String str = "{";
        for (IrStatement stat : statements) {
            str += "\n" + stat.toString();
        }
        str += "\n}";
        return str;
    }
}
