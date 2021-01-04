package codegen;

import java.util.ArrayList;
import java.util.List;

import codegen.Instructions.*;
import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Expression.IrBinaryExpression.BinaryOperator;
import ir.Statement.*;
import semantic.MethodDescriptor;

/**
 * @author Nicola
 */
public class InstructionAssembler implements IrVisitor<List<LIR>> {
    
    private static int strInd = 0;
    private MethodDescriptor method;
    private Exp destLocation;
    
    public InstructionAssembler(MethodDescriptor method) {
        this.method = method;
    }
    
    public void setDestination(Location dest) {
        this.destLocation = dest;
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
        
        // Get assignment destination
        Exp destination = this.destLocation;
                
        // Assemble left & right hand sides
        instrList.addAll(node.getLHS().accept(this));
        Exp lhs = this.destLocation;
        
        instrList.addAll(node.getRHS().accept(this));
        Exp rhs = this.destLocation;
        
        // Do arithmetics and store in destination
        switch (node.getOp()) {
        case PLUS:
            instrList.addAll(handlePlus(destination, lhs, rhs));
            break;
        case MINUS:
            instrList.addAll(handleMinus(destination, lhs, rhs));
            break;
        case TIMES:
            instrList.addAll(handleTimes(destination, lhs, rhs));
            break;
        case DIVIDE:
        case MOD:
            instrList.addAll(handleDivMod(node.getOp(), destination, lhs, rhs));
            break;
        case AND:
        case OR:
            instrList.addAll(handleBool(node.getOp(), destination, lhs, rhs));
            break;
        case GT:
        case LT:
        case GE:
        case LE:
        case EQ:
        case NEQ:
            instrList.addAll(handleComp(node.getOp(), destination, lhs, rhs));
            break;
        default:
            throw new Error("Unrecognized operation");
        }
            
        this.destLocation = null;
        return instrList;
    }

    @Override
    public List<LIR> visit(IrBooleanLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        if (node.eval()) {
            this.destLocation = new Literal(1, 1);
        } else {
            this.destLocation = new Literal(0, 1);
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
        instrList.add(new Mov(Register.rax(), Register.r11()));
        instrList.add(Register.r11());
        
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

        // Add offset if destination is array element
        if (!node.isArrayElement()) {
            this.destLocation = this.method.getDestination(node);
        } else {
            Global location = new Global((Global) this.method.getDestination(node));
            node.getInd().accept(this);         // Put index location into destLocation field
            
            // Get index into register (if not literal or register already)
            Exp ind = this.destLocation;   
            if (!ind.isReg() && !ind.isLiteral()) {
                instrList.add(new Mov(ind, Register.r11()));
                ind = Register.r10();
            }
            
            // Set destination to global array element
            location.setOffset(ind);
            this.destLocation = location;
            
            // Check array bounds
            instrList.addAll(checkArrayBounds(ind, location.getLen()));
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
        instrList.add(new Mov(Register.rax(), Register.r11()));
        instrList.add(Register.r11());
        
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
        
        instrList.add(r11);
        return instrList;
    }

    @Override
    public List<LIR> visit(IrIntLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        this.destLocation = new Literal(node.eval());
        return instrList;
    }

    @Override
    public List<LIR> visit(IrAssignment node) {
        
        // TODO destination vs location
        List<LIR> instrList = new ArrayList<LIR>();
         
        // Assemble destination and get destination address
        instrList.addAll(node.getLocation().accept(this));
        Exp dest = this.destLocation;

        // Process assignment expression
        instrList.addAll(node.getExpression().accept(this));
        
        // Manually assign if expression is literal
        if (this.destLocation != null) {
            instrList.add(new Mov(this.destLocation, dest));
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
        List<LIR> invokeList = node.getMethod().accept(this);
        invokeList.remove(invokeList.size()-1);
        return invokeList;
    }

    @Override
    public List<LIR> visit(IrReturnStatement node) {
        throw new Error("Not supported");
    }
    
    private String getStringId() {
        return "_" + method.getId() + "_str" + strInd++;
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

            if (i <= 6) {    // Move to register
                Exp dst = Call.getParamAtIndex(i);
                instrList.add(new Mov(src, dst));
            } else {        // Move to stack
                instrList.add(new Push(src));
            }            
        }
    }
    
    
    private List<LIR> checkArrayBounds(Exp ind, int lenght) {
        List<LIR> checkInstr = new ArrayList<LIR>();
        ErrorHandle boundErr = ErrorHandle.outOfBounds();
        
        checkInstr.add(new Push(Register.r10()));
        
        // Check lower bound
        checkInstr.add(new Mov(ind, Register.r10()));
        checkInstr.add(new Comp(new Literal(0), Register.r10()));
        checkInstr.add(new Jump(boundErr.getLabel(), "lt"));
        
        // Check upper bound
        checkInstr.add(new Mov(ind, Register.r10()));
        checkInstr.add(new Comp(new Literal(lenght), Register.r10()));
        checkInstr.add(new Jump(boundErr.getLabel(), "ge"));
        
        checkInstr.add(new Pop(Register.r10()));        
        checkInstr.add(boundErr);
        
        return checkInstr;
    }
    
    private List<LIR> handlePlus(Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        Register r10 = Register.r10();
        
        // Simplifications
        if (lhs.equals(dest) && rhs.equals(dest)) {
            // a = a + a
            instrList.add(new LShift(dest));
        } else if (lhs.equals(dest)) {
            if (rhs.equals(new Literal(1))) {
                // a = a + 1
                instrList.add(new Inc(dest));
            } else {
                // a = a + c
                if (rhs.isImm() || dest.isImm()) {
                    instrList.add(new BinOp("addq", rhs, dest));
                } else {
                    instrList.add(new Mov(rhs, r10));
                    instrList.add(new BinOp("addq", r10, dest));
                }
            }
        } else if (rhs.equals(dest)) {
            if (lhs.equals(new Literal(1))) {
                // a = 1 + a
                instrList.add(new Inc(dest));
            } else {
                // a = b + a
                if (lhs.isImm() || dest.isImm()) {
                    instrList.add(new BinOp("addq", lhs, dest));
                } else {
                    instrList.add(new Mov(lhs, r10));
                    instrList.add(new BinOp("addq", r10, dest));
                }
            }
        
        // Destination is register
        } else if (dest.isReg()) {
            
            // Both operands immediate
            if (lhs.isImm() && rhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("addq", rhs, dest));
             
            // One operand in memory
            } else if (lhs.isImm()) {
                instrList.add(new Mov(rhs, dest));
                instrList.add(new BinOp("addq", lhs, dest));
            } else if (lhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("addq", rhs, dest));
                
            // Both operands in memory
            } else {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("addq", rhs, dest));
            }
                
        // Destination in memory
        } else {
            
            // Make sure the memory is moved in r10 first
            if (lhs.isImm()) {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new BinOp("addq", lhs, r10));
                instrList.add(new Mov(r10, dest));
            } else {
                instrList.add(new Mov(lhs, r10));
                instrList.add(new BinOp("addq", rhs, r10));
                instrList.add(new Mov(r10, dest));
            }
        }
        return instrList;
    }
    
    private List<LIR> handleMinus(Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        // TODO
        return instrList;
    }
    
    private List<LIR> handleTimes(Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        // TODO
        return instrList;
    }
    
    private List<LIR> handleDivMod(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        // TODO
        return instrList;
    }
    
    private List<LIR> handleBool(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        // TODO
        return instrList;
    }
    
    private List<LIR> handleComp(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        return instrList;
    }

}
