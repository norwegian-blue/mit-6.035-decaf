package codegen;

import cfg.Nodes.*;
import codegen.Instructions.Enter;
import codegen.Instructions.Label;
import codegen.Instructions.NewLine;
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
    
    public CodeGenerator(AssemblyProgram prog, SymbolTable table, String currentMethod) {
        this.prog = prog;
        this.table = table;
        this.currentMethod = currentMethod;
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
        prog.addInstruction(new Label(methodDesc.getId()));   
        if (stackTop > 8) {
            prog.addInstruction(new Enter(stackTop-8));
        }
        
        // Move parameters on stack
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(CfgStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    
    
}
