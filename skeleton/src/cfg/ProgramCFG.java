package cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codegen.AssemblyProgram;
import codegen.Instructions.*;
import ir.Declaration.*;
import semantic.DuplicateKeyException;
import semantic.FieldDescriptor;
import semantic.SymbolTable;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */

public class ProgramCFG {

    private List<IrFieldDeclaration> globals;
    private Map<String, MethodCFG> methods;
    
    public ProgramCFG(IrClassDeclaration program) {
        this.globals = program.getFields();
        this.methods = new HashMap<String, MethodCFG>();
        
        for (IrMethodDeclaration method : program.getMethods()) {
            MethodCFG CFG = CFGCreator.BuildMethodCFG(method);
            CFG.removeNoOps();
            this.methods.put(method.getId(), CFG);            
        }
    }
    
    public void flatten() {
        for (String methodName : methods.keySet()) {
            methods.get(methodName).flatten();
        }
    }
    
    public void blockify() {
        for (String method: methods.keySet()) {
            methods.put(method, methods.get(method).blockify());
        }
    }
    
    public AssemblyProgram assemble() {
        AssemblyProgram prog = new AssemblyProgram();
        SymbolTable table = new SymbolTable();
        
        // Add globals
        for (IrFieldDeclaration glb : globals) {
            prog.addGlobal(new Global(glb.getId(), glb.getType().getLength(), glb.getType().getSize()));
            try {
                table.put(glb.getId(), new FieldDescriptor(glb.getId(), glb.getType()));
            } catch (DuplicateKeyException e) {
                throw new Error("unexpected behavior");
            }
        }
        
        // Add methods
        for (String method : methods.keySet()) {
            methods.get(method).assemble(prog, table);
        }
            
        return prog;
    }
    
    @Override 
    public String toString() {
        String graph = "";
        for (String method : methods.keySet()) {
            graph += method + ":\n" + methods.get(method).toString() + "\n\n";
        }
        return graph;
    }
    
}
