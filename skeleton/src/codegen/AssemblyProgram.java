package codegen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import codegen.Instructions.*;

/**
 * @author Nicola
 */
public class AssemblyProgram {
    
    private List<LIR> instructions;
    private List<Global> globals;
    private List<StringLiteral> strings;
    
    public AssemblyProgram() {
        instructions = new ArrayList<LIR>();
        globals = new ArrayList<Global>();
        strings = new ArrayList<StringLiteral>();   
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
    
    public String toCode() {
        String prog;
        prog = "\t.section .text\n";
        
        for (LIR inst : instructions) {
            prog += inst.toCode() + "\n";
        }
        
        for (StringLiteral str : strings) {
            prog += "\n" + str.toAllocation() + "\n";
        }
        
        for (Global glb : globals) {
            prog += "\n" + glb.toAllocation() + "\n";
        }
        
        return prog;
    }
    
    public void printToFile(PrintStream os) {
        os.println(this.toCode());
    }
}
