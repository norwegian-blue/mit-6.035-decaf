package codegen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import cfg.ProgramCFG;
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
        
//        StringLiteral helloWorld = new StringLiteral("Hello, World.\\n", "hello");
//        
//        strings.add(helloWorld);
//        
//        instructions.add(new Label("main"));
//        instructions.add(new Enter(0));
//        instructions.add(new Mov(helloWorld, new Register(Register.Registers.rdi)));
//        instructions.add(new Mov(new Literal(0), new Register(Register.Registers.rax)));
//        instructions.add(new Call("printf"));
//        instructions.add(new Mov(new Literal(0), new Register(Register.Registers.rax)));
//        instructions.add(new Leave());
//        instructions.add(new Return());
    
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
        prog += "\t.globl main\n";
        
        for (LIR inst : instructions) {
            if (inst.isLabel()) {
                prog += inst.toCode();
            } else {
                prog += "\t" + inst.toCode();
            }
            prog += "\n";
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
