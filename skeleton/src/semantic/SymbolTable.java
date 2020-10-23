package semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Nicola
 */
public class SymbolTable {
    
    private Stack<String> stack;
    private Map<String, Bucket<Descriptor>> table;
    private final static String envMarker = "#";
    
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
     * @throws DuplicateKeyException if symbol is already bound in current environment 
     */
    public boolean put(String symbol, Descriptor binding) throws DuplicateKeyException {
        
        boolean isBound = table.containsKey(symbol);
        
        if (isBound && this.isInScope(symbol)) {
            throw new DuplicateKeyException(symbol + " is already defined");
        }
        
        if (isBound) {
            table.put(symbol, new Bucket<Descriptor>(symbol, binding, table.get(symbol)));
            
        } else {
            table.put(symbol, new Bucket<Descriptor>(symbol, binding, null));
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
        stack.push(envMarker);
    }
    
    /**
     * Terminate a scope
     * Undoes the bindings created by a new scope
     */
    public void endScope() {
        String symbol = stack.pop();
        while (!symbol.equals(envMarker)) {
            pop(symbol);
            symbol = stack.pop();
        }
    }
        
    /**
     * Removes latest binding for symbol (if present) or directly removes it from table
     */
    private Descriptor pop(String symbol) {
        
        Descriptor desc;
        
        try {
            desc = this.get(symbol);
            Bucket<Descriptor> current = table.get(symbol);
            
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
    
    /**
     * Check if symbol is in current scope
     */
    private boolean isInScope(String symbol) {
        
        // symbol defined after current environment start
        return stack.lastIndexOf(symbol) > stack.lastIndexOf(envMarker);
    }
    
    @Override
    public String toString() {
        String str = "SYMBOL_TABLE\n";
        for (String entry : table.keySet()) {
            try {
                str += String.format("%-10s", entry) + " : " + this.get(entry).toString() + "\n";
            } catch (KeyNotFoundException e) {};
        }
        return str;
    }
}