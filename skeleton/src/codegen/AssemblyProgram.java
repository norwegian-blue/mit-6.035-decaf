package codegen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codegen.Instructions.*;

/**
 * @author Nicola
 */
public class AssemblyProgram {
    
    private List<LIR> instructions;
    private List<Global> globals;
    private List<StringLiteral> strings;
    private Set<ErrorHandle> errors;
    
    public AssemblyProgram() {
        instructions = new ArrayList<LIR>();
        globals = new ArrayList<Global>();
        strings = new ArrayList<StringLiteral>();
        errors = new HashSet<ErrorHandle>();
    }
    
    public void addInstruction(LIR newInstruction) {
        instructions.add(newInstruction);
    }
    
    public void addGlobal(Global newGlobal) {
        globals.add(newGlobal);
    }
    
    public void addString(StringLiteral newString) {
        strings.add(newString);
    }
    
    public void addErrorHandler(ErrorHandle err) {
        errors.add(err);
    }
    
    public String toCode() {
        String prog;
        prog = "\t.section .text\n";
        
        // Methods
        for (LIR inst : instructions) {
            prog += inst.toCode() + "\n";
        }
        
        // Error handlers
        for (ErrorHandle err : errors) {
            this.addString(err.getErrorMsg());
            prog += "\n" + err.toCode() + "\n";
        }
        
        // Strings
        for (StringLiteral str : strings) {
            prog += "\n" + str.toAllocation() + "\n";
        }
        
        // Globals
        for (Global glb : globals) {
            prog += "\n" + glb.toAllocation() + "\n";
        }
        
        return prog;
    }
    
    public void printToFile(PrintStream os) {
        os.println(this.toCode());
    }
}
