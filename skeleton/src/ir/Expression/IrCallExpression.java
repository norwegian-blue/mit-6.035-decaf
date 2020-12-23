package ir.Expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public abstract class IrCallExpression extends IrExpression {
    
    private List<IrExpression> args;
    private String name;
    
    public IrCallExpression(String name, List<IrExpression> args) {
        this.name = name;
        this.args = args;
    } 
    
    public boolean returnsVal() {
        return this.getExpType() != BaseTypeDescriptor.VOID; 
    }
    
    public List<IrExpression> getArgs() {
        return this.args;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public Set<IrIdentifier> getUsedVars() {
        Set<IrIdentifier> vars = new HashSet<IrIdentifier>();
        for (IrExpression arg : args) {
            vars.addAll(arg.getUsedVars());
        }
        return vars;
    }
}