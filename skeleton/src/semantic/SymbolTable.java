package semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Nicola
 */
public class SymbolTable {
    
    private Stack<String> stack;
    //private Stack<Descriptor> scopeStack;
    private Map<String, Bucket> table;
    
    public SymbolTable(){
        stack = new Stack<>();
        table = new HashMap<>();
    }
    
    /** 
     * Add a symbol descriptor to table.
     * If the symbol is already available, the new binding will shadow the previous existing one
     * The binding can be undone by popping the symbol later
     * @param symbol Variable/Method/Class identifier
     * @param binding Descriptor for the identifier
     * @return true if the symbol was already defined in the table
     */
    public boolean put(String symbol, Descriptor binding) {
        
        boolean isBound = table.containsKey(symbol);
        
        if (isBound) {
            table.put(symbol, new Bucket(symbol, binding, table.get(symbol)));
            
        } else {
            table.put(symbol, new Bucket(symbol, binding, null));
        }
        
        stack.push(symbol);
        return isBound;
    }
    
    /**
     * Get symbol descriptor
     * @param symbol Variable/Method/Class identifier
     * @return  A Descriptor object for the symbol
     * @throws KeyNotFoundException in case symbol is not defined in the table
     */
    public Descriptor get(String symbol) throws KeyNotFoundException {
        if (table.containsKey(symbol)) {
            return table.get(symbol).getBinding();
        } else {
            throw new KeyNotFoundException("Identifier " + symbol + " is not defined");
        }            
    }
    
    /**
     * Begin a new scope
     * When a scope is created, new binding can shadow existing bindings in the environment
     * The operation is reversible by the endScope method
     */
    public void beginScope() {
        stack.push("#");
    }
    
    /**
     * Terminate a scope
     * Undoes the bindings created by a new scope
     */
    public void endScope() {
        String symbol = stack.pop();
        while (!symbol.equals("#")) {
            pop(symbol);
            symbol = stack.pop();
        }
    }
    
    /**
     * Push a scope on the stack, to recover it later
     *
    public void pushScope() {
        String symbol = stack.pop();
        while (!symbol.contentEquals("#")) {
            scopeStack.push(pop(symbol));
            symbol = stack.pop();
        }
    }/
    
    /**
     * Pop a scope from the stack, recovering discarded bindings
     *
    public void popScope() {
    }/
    
    /**
     * Removes latest binding for symbol (if present) or directly removes it from table
     */
    private Descriptor pop(String symbol) {
        
        Descriptor desc;
        
        try {
            desc = this.get(symbol);
            Bucket current = table.get(symbol);
            
            if (current.hasNext()) {
                table.replace(symbol, current.getNext());
            } else {
                table.remove(symbol);
            } 
        } catch (KeyNotFoundException e) { 
            throw new Error("Identifier " + symbol + " is not defined");
        }
        
        return desc;
    }
}