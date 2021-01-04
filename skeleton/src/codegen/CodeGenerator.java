package codegen;

import java.util.Collections;
import java.util.List;

import cfg.Nodes.*;
import codegen.Instructions.*;
import ir.Expression.IrExpression;
import ir.Expression.IrIdentifier;
import ir.Expression.IrLiteral;
import ir.Statement.*;
import semantic.BaseTypeDescriptor;
import semantic.LocalDescriptor;
import semantic.MethodDescriptor;
import semantic.ParameterDescriptor;

/**
 * @author Nicola
 */

public class CodeGenerator implements NodeVisitor<Void> {
    
    private AssemblyProgram prog;
    private InstructionAssembler instructionAssembler;
    private boolean isFirst;
    private MethodDescriptor method;
    
    public CodeGenerator(AssemblyProgram prog, MethodDescriptor method) {
        this.prog = prog;
        this.instructionAssembler = new InstructionAssembler(method);
        this.method = method;
        isFirst = true;
    }

    @Override
    public Void visit(CfgBlock node) {
        
        // TODO
        
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
            this.method.setCurrentNode(atomNode);
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
        
        // TODO
        
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
                
        // Set stack: assign storage for locals and parameters if needed
        method.setStack();
        
        // Update locations
        method.updateLocations(node);        

        // Preamble
        prog.addInstruction(new NewLine());
        if (method.getId().equals("main")) {
            prog.addInstruction(new MainDirective());
        }
        prog.addInstruction(new Label(method.getId()));   
        prog.addInstruction(new Enter(method.getStackTop()-8));
        
        // Push callee saved registers on stack
        for (Register reg : method.getUsedRegs()) {
            if (method.getId().equals("main")) continue;
            if (reg.equals(Register.rbx())) {
                prog.addInstruction(new Push(reg));
            } else if (reg.equals(Register.r12())) {
                prog.addInstruction(new Push(reg));
            } else if (reg.equals(Register.r13())) {
                prog.addInstruction(new Push(reg));
            } else if (reg.equals(Register.r14())) {
                prog.addInstruction(new Push(reg));
            } else if (reg.equals(Register.r15())) {
                prog.addInstruction(new Push(reg));
            }            
        }
                
        // Move parameters onto local storage (or to registers if allocated)   
        int i = 0;
        for (ParameterDescriptor par : method.getPars()) {
            Location parLocal = method.getDestination(par.getIrId());
            Location parSrc = Call.getParamAtIndex(++i, par.getSize());
            
            // Move if source != local
            if (!parSrc.equals(parLocal)) {
                if (!parSrc.isReg() && !parLocal.isReg()) {
                    prog.addInstruction(new Mov(parSrc, Register.r10()));
                    prog.addInstruction(new Mov(Register.r10(), parLocal));
                } else {
                    prog.addInstruction(new Mov(parSrc, parLocal));
                }
            }
        }
        
        // Initialize locals to zero
        for (LocalDescriptor local : method.getLocals()) {
            if (method.isLive(local.getIrId())) {
                Location varLocal = method.getLocation(local.getIrId());
                prog.addInstruction(new Mov(new Literal(0, local.getSize()), varLocal));
            }
        }

        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        
        // Update locations
        method.updateLocations(node);
                
        // Return 0 on main successful exit or return value if needed
        // also check if control may be falling off
        boolean falloff = false;
        if (method.getId().equals("main")) {
            prog.addInstruction(new Mov(new Literal(0), Register.rax()));
        } else if (method.getType() != BaseTypeDescriptor.VOID) {
            if (node.returnsExp()) {
                // Move to return address
                IrExpression exp = node.getExp();
                if (exp.isLiteral()) {
                    IrLiteral val = (IrLiteral) exp;
                    Literal lit = val.getLiteral();
                    prog.addInstruction(new Mov(lit, Register.rax()));
                } else {
                    IrIdentifier id = (IrIdentifier) exp.getUsedVars().toArray()[0];
                    Location src = method.getLocation(id);
                    if (!src.equals(Register.rax())) {
                        prog.addInstruction(new Mov(src, Register.rax()));
                    }
                }
            } else {
                falloff = true;
            }
        }
                
        // Restore stack
        List<Register> regs = method.getUsedRegs();
        Collections.reverse(regs);
        for (Register reg : regs) {
            if (method.getId().equals("main")) continue;
            if (reg.equals(Register.rbx())) {
                prog.addInstruction(new Pop(reg));
            } else if (reg.equals(Register.r12())) {
                prog.addInstruction(new Pop(reg));
            } else if (reg.equals(Register.r13())) {
                prog.addInstruction(new Pop(reg));
            } else if (reg.equals(Register.r14())) {
                prog.addInstruction(new Pop(reg));
            } else if (reg.equals(Register.r15())) {
                prog.addInstruction(new Pop(reg));
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
        
        // Update locations
        method.updateLocations(node);
        
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
