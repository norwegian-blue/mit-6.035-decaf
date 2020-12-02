package codegen;

import java.util.List;

import cfg.Nodes.*;
import codegen.Instructions.*;
import ir.Expression.IrExpression;
import ir.Statement.*;
import semantic.BaseTypeDescriptor;
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
    private boolean isFirst;
    
    public CodeGenerator(AssemblyProgram prog, SymbolTable table, String currentMethod) {
        this.prog = prog;
        this.table = table;
        this.currentMethod = currentMethod;
        this.instructionAssembler = new InstructionAssembler(table, currentMethod);
        isFirst = true;
    }

    @Override
    public Void visit(CfgBlock node) {
        
        // Skip if visited already
        if (node.isVisited()) {
            prog.addInstruction(new Jump(node.getBlockName(), "none"));
            return null;
        }
        node.visit();
        
        // Do not label if first
        if (!isFirst) {
            prog.addInstruction(new Label(node.getBlockName()));
        } else {
            isFirst = false;
        }        

        // Assemble operations
        for (Node atomNode : node.getBlockNodes()) {
            atomNode.accept(this);
        }
        
        // Handle transition
        if (node.isFork()) {
            CfgBlock trueBlock = node.getTrueBlock();
            CfgBlock falseBlock = node.getFalseBlock();
            prog.addInstruction(new Mov(new Literal(1), Register.r10()));
            prog.addInstruction(new Comp(Register.r10(), Register.r11()));
            prog.addInstruction(new Jump(trueBlock.getBlockName(), "eq"));
            falseBlock.accept(this);
            if (!trueBlock.isVisited()){
                trueBlock.accept(this);
            }
        } else if (node.hasNext()) {
            CfgBlock nextBlock = node.getNextBlock();
            nextBlock.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CfgCondBranch node) {
        
        IrExpression exp = node.getCond();
        List<LIR> instructions = exp.accept(instructionAssembler);
        if (instructions.size() > 1) {
            instructions.remove(instructions.size()-1);
        } else {
            Exp var = (Exp)instructions.get(0);
            instructions.set(0, new Mov(var, Register.r11()));
        }
        
        for (LIR instr : instructions) {
            this.prog.addInstruction(instr);
        }
        
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
                table.get(parDesc.getId()).setOffset(-stackTop);
            } catch (KeyNotFoundException e) {
                throw new Error("unexpected error");
            }
            stackTop += parDesc.getSize();
        }
               
        // Assign storage for locals
        for (LocalDescriptor local : methodDesc.getLocals()) {
            try {
                table.get(local.getId()).setOffset(-stackTop);
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
            if (parSrc.isReg()) {
                prog.addInstruction(new Mov(parSrc, parLocal));
            } else {
                prog.addInstruction(new Mov(parSrc, Register.r11()));
                prog.addInstruction(new Mov(Register.r11(), parLocal));
            }
        }
        
        // Initialize locals to zero
        for (LocalDescriptor local : methodDesc.getLocals()) {
            Local varLocal = new Local(local.getOffset());
            prog.addInstruction(new Mov(new Literal(0), varLocal));
        }
        
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        
        // Get current method descriptor
        MethodDescriptor methodDesc;
        try {
            methodDesc = (MethodDescriptor) table.get(currentMethod);
        } catch (KeyNotFoundException e) { 
            throw new Error("unexpected error");
        }
        
        // Return 0 on main successful exit or return value if needed
        // also check if control may be falling off
        boolean falloff = false;
        if (methodDesc.getId().equals("main")) {
            prog.addInstruction(new Mov(new Literal(0), Register.rax()));
        } else if (methodDesc.getType() != BaseTypeDescriptor.VOID) {
            if (node.returnsExp()) {
                List<LIR> instructions = node.getReturnExp().accept(instructionAssembler);
                Exp src = (Exp)instructions.get(0);
                prog.addInstruction(new Mov(src, Register.rax()));
            } else {
                falloff = true;
            }
        }
        
        // Return to caller or throw runtime error
        if (falloff) {
            ErrorHandle fall = ErrorHandle.fallOver();
            prog.addInstruction(new Jump(fall.getLabel(), "none"));
            prog.addErrorHandler(fall);
        } else {
            prog.addInstruction(new Leave());
            prog.addInstruction(new Return());
        }
        
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
            } else if (instr.isErrorHandler()) {
                this.prog.addErrorHandler((ErrorHandle)instr);
            } else {
                this.prog.addInstruction(instr);
            }
        }
        
        return null;
    }
    
}
