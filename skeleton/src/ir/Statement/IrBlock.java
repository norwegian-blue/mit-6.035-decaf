package ir.Statement;

import java.util.ArrayList;
import java.util.List;

import ir.IrVisitor;
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
    
    public List<IrVariableDeclaration> getVarDecl() {
        if (this.variableDec != null) {
            return this.variableDec;
        } else {
            return new ArrayList<>();
        }
    }
    
    public List<IrStatement> getStatements() {
        if (this.statements != null) {
            return this.statements;
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override 
    public String toString() {
        String str = "{";
        if (variableDec != null) {
            for (IrVariableDeclaration var : this.variableDec) {
                str += "\n" + var.toString();
            }
        }
        if (!isempty) {
            for (IrStatement stat : this.statements) {
                str += "\n" + stat.toString();
            }
        }
        str += "\n}";
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
