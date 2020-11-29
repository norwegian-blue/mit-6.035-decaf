package codegen;

import java.util.List;

import cfg.Nodes.*;
import codegen.Instructions.*;
import ir.Statement.*;
import semantic.KeyNotFoundException;
import semantic.LocalDescriptor;
import semantic.MethodDescriptor;
import semantic.ParameterDescriptor;
import semantic.SymbolTable;

/**
 * @author Nicola
 */

public class CodeGenerator implements NodeVisitor<Void> {
    
    private AssemblyProgram prog;
    private SymbolTable table;
    private String currentMethod;
    private InstructionAssembler instructionAssembler;
    
    public CodeGenerator(AssemblyProgram prog, SymbolTable table, String currentMethod) {
        this.prog = prog;
        this.table = table;
        this.currentMethod = currentMethod;
        this.instructionAssembler = new InstructionAssembler(table, currentMethod);
    }

    @Override
    public Void visit(CfgBlock node) {
        
        for (Node atomNode : node.getBlockNodes()) {
            atomNode.accept(this);
        }
        
        // TODO Handle out transition
        return null;
    }

    @Override
    public Void visit(CfgCondBranch node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(CfgEntryNode node) {
        
        // Get current method descriptor
        MethodDescriptor methodDesc;
        try {
            methodDesc = (MethodDescriptor) table.get(currentMethod);
        } catch (KeyNotFoundException e) { 
            throw new Error("unexpected error");
        }
        
        // Assign variables location on stack
        int stackTop = 8;
        
        // Assign storage for parameters
        for (ParameterDescriptor parDesc : methodDesc.getPars()) {
            try {
                table.get(parDesc.getId()).setOffset(stackTop);
            } catch (KeyNotFoundException e) {
                throw new Error("unexpected error");
            }
            stackTop += parDesc.getSize();
        }
               
        // Assign storage for locals
        for (LocalDescriptor local : methodDesc.getLocals()) {
            try {
                table.get(local.getId()).setOffset(stackTop);
            } catch (KeyNotFoundException e) {
                throw new Error("unexpected error");
            }
            stackTop += local.getSize();
        }

        // Preamble
        prog.addInstruction(new NewLine());
        if (methodDesc.getId().equals("main")) {
            prog.addInstruction(new MainDirective());
        }
        prog.addInstruction(new Label(methodDesc.getId()));   
        prog.addInstruction(new Enter(stackTop-8));
        
        // Move parameters on stack
        int i = 0;
        for (ParameterDescriptor par : methodDesc.getPars()) {
            Local parLocal = new Local(par.getOffset());
            Exp parSrc = Call.getParamAtIndex(++i);
            prog.addInstruction(new Mov(parSrc, parLocal));
        }
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        // TODO return value
        
        // Get current method descriptor
        MethodDescriptor methodDesc;
        try {
            methodDesc = (MethodDescriptor) table.get(currentMethod);
        } catch (KeyNotFoundException e) { 
            throw new Error("unexpected error");
        }
        
        // Return 0 on main successful exit
        if (methodDesc.getId().equals("main")) {
            prog.addInstruction(new Mov(new Literal(0), new Register(Register.Registers.rax)));
        }
        
        // Return to caller
        prog.addInstruction(new Leave());
        prog.addInstruction(new Return());
        
        return null;
    }

    @Override
    public Void visit(CfgStatement node) {
        
        IrStatement stat = node.getStatement();
        List<LIR> instructions = stat.accept(instructionAssembler);
        
        if (instructions == null) {
            return null;
        }
        
        for (LIR instr : instructions) {
            if (instr.isString()) {
                this.prog.addString((StringLiteral)instr);
            } else {
                this.prog.addInstruction(instr);
            }
        }
        
        return null;
    }

    
    
}
