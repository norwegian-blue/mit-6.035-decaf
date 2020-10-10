package ir.Statement;

import java.util.List;
import ir.Declaration.*;

/**
 * @author Nicola
 */
public class IrBlock extends IrStatement {
    private final List<IrStatement> statements;
    private final List<IrVariableDeclaration> variableDec;
    private final boolean isempty;
    
    public IrBlock(List<IrVariableDeclaration> variables, List<IrStatement> statements) {
        this.statements = statements;
        this.variableDec = variables;
        this.isempty = false;
    }
        
    public IrBlock(List<IrStatement> statements) {
        this.statements = statements;
        this.variableDec = null;
        this.isempty = false;
    }
    
    public IrBlock() {
        this.isempty = true;
        this.statements = null;
        this.variableDec = null;
    }
    
    public boolean isEmpty() {
        return isempty;
    }
    
    @Override 
    public String toString() {
        String str = "{";
        for (IrVariableDeclaration var : this.variableDec) {
            str += "\n" + var.toString();
        }
        for (IrStatement stat : this.statements) {
            str += "\n" + stat.toString();
        }
        str += "\n}";
        return str;
    }
}
