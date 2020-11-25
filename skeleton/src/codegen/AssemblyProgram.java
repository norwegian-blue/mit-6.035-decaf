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
        
        StringLiteral helloWorld = new StringLiteral("Hello world!\\n", "hello");
        
        strings.add(helloWorld);
        
        instructions.add(new Label("main"));
        instructions.add(new Enter(0));
        instructions.add(new Mov(helloWorld, new Register(Register.Registers.rdi)));
        instructions.add(new Mov(new Literal(0), new Register(Register.Registers.rax)));
        instructions.add(new Call("printf"));
        instructions.add(new Mov(new Literal(0), new Register(Register.Registers.rax)));
        instructions.add(new Leave());
        instructions.add(new Return());
    
    }
    
    public String toCode() {
        String prog;
        prog = "\t.section .text";
        prog += "\n\t.globl main\n";
        
        for (LIR inst : instructions) {
            prog += "\n";
            if (inst.isLabel()) {
                prog += inst.toCode();
            } else {
                prog += "\t" + inst.toCode();
            }
        }
        
        for (StringLiteral str : strings) {
            prog += "\n\n" + str.toAllocation();
        }
        
        for (Global glb : globals) {
            prog += "\n\n" + glb.toAllocation();
        }
        
        return prog;
    }
    
    public void printToFile(PrintStream os) {
        os.println(this.toCode());
    }
}
