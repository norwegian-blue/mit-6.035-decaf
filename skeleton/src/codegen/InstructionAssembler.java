package codegen;

import java.util.ArrayList;
import java.util.List;

import codegen.Instructions.*;
import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Expression.IrBinaryExpression.BinaryOperator;
import ir.Statement.*;
import semantic.Descriptor;
import semantic.KeyNotFoundException;
import semantic.SymbolTable;

/**
 * @author Nicola
 */
public class InstructionAssembler implements IrVisitor<List<LIR>> {
    
    private final SymbolTable table;
    private final String currentMethod;
    private static int strInd = 0;
    
    public InstructionAssembler(SymbolTable table, String currentMethod) {
        this.table = table;
        this.currentMethod = currentMethod;
    }

    @Override
    public List<LIR> visit(IrClassDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrFieldDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrMethodDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrParameterDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrVariableDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrBinaryExpression node) {
        List<LIR> instrList = new ArrayList<LIR>();
        Register r10 = Register.r10();
        Register r11 = Register.r11();
        
        // Assemble left & right hand sides
        List<LIR> lhs = node.getLHS().accept(this);
        List<LIR> rhs = node.getRHS().accept(this);
        
        // Move operands to registers %r10 and %r11
        Exp lhsSrc = (Exp) lhs.get(0);
        Exp rhsSrc = (Exp) rhs.get(0);
        
        instrList.add(new Mov(lhsSrc, r10));
        instrList.add(new Mov(rhsSrc, r11));
        
        // Do arithmetics and store results in %r11        
        switch (node.getOp()) {
        case PLUS:           
            instrList.add(new BinOp("add", r10, r11));
            break;
        case MINUS:
            instrList.add(new BinOp("sub", r11, r10));
            instrList.add(new Mov(r10, r11));
            break;
        case TIMES:
            instrList.add(new BinOp("imul", r10, r11));
            break;
        case DIVIDE:
        case MOD:
            instrList.add(new Mov(new Literal(0), Register.rdx()));
            instrList.add(new Mov(r10, Register.rax()));
            instrList.add(new Command("cqto"));
            instrList.add(new UnOp("idiv", r11));
            if (node.getOp().equals(BinaryOperator.DIVIDE)) {
                instrList.add(new Mov(Register.rax(), r11));
            }else {
                instrList.add(new Mov(Register.rdx(), r11));
            }
            break;
        case AND:
            instrList.add(new BinOp("and", r10, r11));
            break;
        case OR:
            instrList.add(new BinOp("or", r10, r11));
            break;
        case GT:
        case LT:
        case GE:
        case LE:
        case EQ:
        case NEQ:
            instrList.add(new Comp(r11, r10));
            instrList.add(new Mov(new Literal(1), r10));
            instrList.add(new Mov(new Literal(0), r11));
            instrList.add(new CMov(node.getOp(), r10, r11));
            break;
        default:
            throw new Error("Unrecognized operation");
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrBooleanLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        if (node.eval()) {
            instrList.add(new Literal(1));
        } else {
            instrList.add(new Literal(0));
        }
        return instrList;
    }

    @Override
    public List<LIR> visit(IrCalloutExpression node) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        String calloutName = node.getName();
        calloutName = calloutName.substring(1, calloutName.length()-1);
        
        // Set %rax to 0 if calling printf function
        if (calloutName.equals("printf")) {
            instrList.add(new Mov(new Literal(0), Register.rax()));
        }
        
        // Prepare arguments
        this.prepareFunCall(node.getArgs(), instrList);
                
        // Call function
        instrList.add(new Call(calloutName));
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrStringLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        instrList.add(new StringLiteral(node.eval(), this.getStringId()));
        return instrList;
    }

    @Override
    public List<LIR> visit(IrIdentifier node) {
        List<LIR> instrList = new ArrayList<LIR>();
        if (node.isArrayElement()) {
            // TODO handle array
            return null;
        } else {
            Descriptor nodeDesc;
            try {
                nodeDesc = table.get(node.getId());
                instrList.add(new Local(nodeDesc.getOffset()));
            } catch (KeyNotFoundException e) {
                throw new Error("Unexpected exception");
            }
        }  
        return instrList;
    }

    @Override
    public List<LIR> visit(IrMethodCallExpression node) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        String methodName = node.getName();
        
        // Prepare arguments
        this.prepareFunCall(node.getArgs(), instrList);
                
        // Call function
        instrList.add(new Call(methodName));
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrUnaryExpression node) {
        List<LIR> instrList = new ArrayList<LIR>();
        Register r11 = Register.r11();
        
        // Assemble right hand sides
        List<LIR> exp = node.getExp().accept(this);
        
        // Move operand to registers %r11
        Exp expSrc = (Exp) exp.get(0);
        instrList.add(new Mov(expSrc, r11));
        
        // Do arithmetics and store results in %r11        
        switch (node.getOp()) {
        case MINUS:
            instrList.add(new UnOp("neg", r11));
            break;
        case NOT:
            instrList.add(new BinOp("xorq", new Literal(1), r11));
            break;
        default:
            throw new Error("Unexpected operator");
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrIntLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        instrList.add(new Literal(node.eval()));
        return instrList;
    }

    @Override
    public List<LIR> visit(IrAssignment node) {
        
        // TODO support different assignments
        List<LIR> instrList = new ArrayList<LIR>();
        
        List<LIR> expList = node.getExpression().accept(this);
        List<LIR> destList = node.getLocation().accept(this);
        
        // Source --> either a value or the result of an operation (stored in r11)
        Exp src;
        if (node.getExpression().isAtom()) {
            src = (Exp) expList.get(expList.size()-1);
        } else {
            for (LIR inst : expList) {
                instrList.add(inst);
            }
            src = Register.r11();
        }
        
        // Assign to local or global (array) 
        Exp dst;
        if (node.getLocation().isAtom()) {
            dst = (Exp) destList.get(destList.size()-1);
            
            if (node.getExpression().isAtom()) {
                instrList.add(new Mov(src, Register.r11()));
                instrList.add(new Mov(Register.r11(), dst));
            } else {
                instrList.add(new Mov(src, dst));
            }   
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrBlock node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrBreakStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrContinueStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrForStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrIfStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public List<LIR> visit(IrInvokeStatement node) {
        return node.getMethod().accept(this);
    }

    @Override
    public List<LIR> visit(IrReturnStatement node) {
        throw new Error("Not supported");
    }
    
    private String getStringId() {
        return "_" + this.currentMethod + "_str" + strInd++;
    }
    
    private void prepareFunCall(List<IrExpression> args, List<LIR> instrList) {
        
        // Move arguments on stack
        for (int i = args.size(); i > 0; i--) {
            List<LIR> argInst = args.get(i-1).accept(this);
            Exp src = (Exp) argInst.get(0);

            // Return string arguments
            if (src.isString()) {
                instrList.add(src);
            }

            if (i < 6) {    // Move to register
                Exp dst = Call.getParamAtIndex(i);
                instrList.add(new Mov(src, dst));
            } else {        // Move to stack
                instrList.add(new Push(src));
            }            
        }
    }

}
