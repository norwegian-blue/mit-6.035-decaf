package semantic.Checker;

import ir.*;
import semantic.SymbolTable;

/**
 * @author Nicola
 */
public abstract class SemanticRule extends IrVisitor<SemanticError> {
    
    private final SymbolTable env;
    
    public SemanticRule(SymbolTable env) {
        this.env = env;
    }
        
    @Override
    public SemanticError visit(Ir exp) {
        return SemanticError.NoError;
    }
    
    public SymbolTable getEnv() {
        return this.env;
    }
        
}
