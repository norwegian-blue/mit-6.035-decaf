package codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    private Exp destination;
    private Exp location;
    private Exp jmpCond = Register.r10();
    
    public InstructionAssembler(MethodDescriptor method) {
        this.method = method;
    }
    
    public void setJmpCond(Exp exp) {
        this.jmpCond = exp;
    }
    
    public Exp getJmpCond() {
        return this.jmpCond;
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
        Exp destination = this.destination;
                
        // Assemble left & right hand sides
        instrList.addAll(node.getLHS().accept(this));
        Exp lhs = this.location;
        
        instrList.addAll(node.getRHS().accept(this));
        Exp rhs = this.location;
        
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
            
        this.location = null;
        return instrList;
    }

    @Override
    public List<LIR> visit(IrBooleanLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        Literal value = (node.eval()) ? new Literal(1, 1) : new Literal(0, 1);
        this.location = value;
        
        // Set jmp conditions
        if (this.jmpCond == null) {
            this.jmpCond = value;
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrCalloutExpression node) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        String calloutName = node.getName();
        calloutName = calloutName.substring(1, calloutName.length()-1);
        boolean updateJump = (this.jmpCond == null);
        
        // Check if value is returned to %rax
        boolean skipRax = (destination != null && destination.equals(Register.rax()));
        
        // Push live registers (non-callee-saved)
        instrList.addAll(saveRegs(skipRax));
                
        // Prepare arguments
        this.prepareFunCall(node.getArgs(), instrList);
        
        // Set %rax to 0 if calling printf function
        if (calloutName.equals("printf")) {
            instrList.add(new Mov(new Literal(0), Register.rax()));
        }
                
        // Call function
        instrList.add(new Call(calloutName));
        
        // Remove extra parameters from stack if more than 6 arguments
        instrList.addAll(cleanupStack(node.getArgs()));
        
        // Return value
        if (destination != null && !destination.equals(Register.rax())) {
            instrList.add(new Mov(Register.rax(), this.destination));   
        }
        
        // Jmp condition
        if (updateJump) {
            if (method.isLive(Register.rax())) {
                instrList.add(new Mov(Register.rax(), Register.r10()));
                jmpCond = Register.r10();
            } else {
                jmpCond = Register.rax();
            }
        }
        
        // Pop live registers (non-callee-saved)
        instrList.addAll(restoreRegs(skipRax));
        
        this.location = null;
        
        return instrList;
    }

    private List<LIR> saveRegs(boolean skipRax) {
        List<LIR> instrList = new ArrayList<LIR>();
        List<Register> calleeSaved = Call.getCalleeSaved();
        
        for (Register reg : calleeSaved) {
            // Do not save register if it is used to store result
            if (this.destination != null && this.destination.isReg() && reg.equals(new Register((Register) destination, 8))) {
                continue;
            }
            if (this.method.isLive(reg)) instrList.add(new Push(reg));
        }

        return instrList;
    }

    private List<LIR> restoreRegs(boolean skipRax) {
        List<LIR> instrList = new ArrayList<LIR>();
        List<Register> calleeSaved = Call.getCalleeSaved();
        Collections.reverse(calleeSaved);
        
        for (Register reg : calleeSaved) {
            // Do not save register if it is used to store result
            if (this.destination != null && this.destination.isReg() && reg.equals(new Register((Register) destination, 8))) {
                continue;
            }
            if (this.method.isLive(reg)) instrList.add(new Pop(reg));
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrStringLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        this.location = new StringLiteral(node.eval(), this.getStringId());
        return instrList;
    }

    @Override
    public List<LIR> visit(IrIdentifier node) {
        List<LIR> instrList = new ArrayList<LIR>();

        // Add offset if destination is array element
        if (!node.isArrayElement()) {
            this.location = this.method.getLocation(node);
        } else {
            Global glb = new Global((Global) this.method.getLocation(node));
            node.getInd().accept(this);         // Put index location into location field
            
            // Get index into register (if not register already)
            Exp ind = this.location;   
            if (!ind.isReg()) {
                instrList.add(new Mov(ind, Register.r11()));
                ind = Register.r11();
            }
            
            // Set destination to global array element
            glb.setOffset(ind);
            this.location = glb;
            
            // Check array bounds
            instrList.addAll(checkArrayBounds(ind, glb.getLen()));
        }
        
        // Jump condition
        if (this.jmpCond == null) {
            this.jmpCond = this.location;
        }
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrMethodCallExpression node) {
        
        List<LIR> instrList = new ArrayList<LIR>();
        
        String methodName = node.getName();
        
        boolean updateJump = (this.jmpCond == null);
        
        // Check if value is returned to %rax
        boolean skipRax = (destination != null && destination.equals(Register.rax()));
        
        // Push live registers (non-callee-saved)
        instrList.addAll(saveRegs(skipRax));
        
        // Prepare arguments
        this.prepareFunCall(node.getArgs(), instrList);
                
        // Call function
        instrList.add(new Call(methodName));
        
        // Remove extra parameters from stack if more than 6 arguments
        instrList.addAll(cleanupStack(node.getArgs()));
        
        // Return value
        if (destination != null && !destination.equals(Register.rax())) {
            instrList.add(new Mov(Register.rax(), this.destination));   
        }
        
        // Jmp condition
        if (updateJump) {
            if (method.isLive(Register.rax())) {
                instrList.add(new Mov(Register.rax(), Register.r10()));
                jmpCond = Register.r10();
            } else {
                jmpCond = Register.rax();
            }
        }
        
        // Pop live registers (non-callee-saved)
        instrList.addAll(restoreRegs(skipRax));
        
        this.location = null;
        
        return instrList;
    }

    @Override
    public List<LIR> visit(IrUnaryExpression node) {
        
        List<LIR> instrList = new ArrayList<LIR>();
        
        Register r10 = Register.r10();
        
        // Get assignment destination
        Exp destination = this.destination;
                
        // Assemble left & right hand sides
        instrList.addAll(node.getExp().accept(this));
        Exp exp = this.location;
        
        // Move exp to destination
        if (!exp.equals(destination)) {
            if (destination.isReg() || exp.isImm()) {
                instrList.add(new Mov(exp, destination));
            } else {
                instrList.add(new Mov(exp, r10));
                instrList.add(new Mov(r10, destination));
            }
        }
        
        // Do arithmetics on destination        
        switch (node.getOp()) {
        case MINUS:
            instrList.add(new UnOp("neg", destination));
            break;
        case NOT:
            instrList.add(new BinOp("xor", new Literal(1), destination));
            break;
        default:
            throw new Error("Unexpected operator");
        }
        
        this.location = null;
        return instrList;
    }

    @Override
    public List<LIR> visit(IrIntLiteral node) {
        List<LIR> instrList = new ArrayList<LIR>();
        this.location = new Literal(node.eval());
        return instrList;
    }

    @Override
    public List<LIR> visit(IrAssignment node) {
        
        List<LIR> instrList = new ArrayList<LIR>();
         
        // Assemble destination and get destination address
        instrList.addAll(setDestination(node.getLocation()));

        // Process assignment expression
        instrList.addAll(node.getExpression().accept(this));
        
        // Manually assign if expression is literal or move instruction
        if (this.location != null) {
            Exp dest = this.destination;
            Exp src = this.location;
            if (src.equals(dest)) {
                // skip
            } else if (!src.isImm() && !dest.isImm()) {
                instrList.add(new Mov(src, Register.r10()));
                instrList.add(new Mov(Register.r10(), dest));
            } else {
                instrList.add(new Mov(src, dest));
            }
        }
        
        this.destination = null;
    
        return instrList;
    }

    private List<LIR> setDestination(IrIdentifier id) {
        List<LIR> instrList = new ArrayList<LIR>();

        // Add offset if destination is array element
        if (!id.isArrayElement()) {
            this.destination = this.method.getDestination(id);
        } else {
            Global glb = new Global((Global) this.method.getDestination(id));
            id.getInd().accept(this);         // Put index location into location field
            
            // Get index into register (if not register already)
            Exp ind = this.location;   
            if (!ind.isReg()) {
                instrList.add(new Mov(ind, Register.r11()));
                ind = Register.r11();
            }
            
            // Set destination to global array element
            glb.setOffset(ind);
            this.destination = glb;
            
            // Check array bounds
            instrList.addAll(checkArrayBounds(ind, glb.getLen()));
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
        return invokeList;
    }

    @Override
    public List<LIR> visit(IrReturnStatement node) {
        throw new Error("Not supported");
    }
    
    private String getStringId() {
        return "_" + method.getId() + "_str" + strInd++;
    }
    
    private List<LIR> cleanupStack(List<IrExpression> args) {
        
        List<LIR> instrList = new ArrayList<LIR>();
        
        // Get list of arguments locations
        List<Exp> argLocs = new ArrayList<Exp>();
        for (IrExpression arg : args) {
            arg.accept(this);
            argLocs.add(this.location);
        }
        
        // Extra offset to be removed
        int offset = 0;
        for (int i = 6; i < argLocs.size(); i++) {
            offset += (argLocs.get(i).getSize() == 1) ? 2 : 8;
        }
        if (offset > 0) {
            instrList.add(new BinOp("add", new Literal(offset), Register.rsp()));
        }
                
        return instrList;
        
    }   
    
    private void prepareFunCall(List<IrExpression> args, List<LIR> instrList) {
        
        // Get list of arguments locations
        List<Exp> argLocs = new ArrayList<Exp>();
        for (IrExpression arg : args) {
            arg.accept(this);
            argLocs.add(this.location);
        }
        
        // Move arguments according to calling convention
        //      1. push on stack arguments over 6
        //      2. move registers to destination (avoid later conflicts)
        //      3. move other parameters to destination (no conflicts with registers)
        
        // 1. Push arg > 6 onto stack
        Collections.reverse(argLocs);
        Iterator<Exp> it = argLocs.iterator();
        int i = argLocs.size();
        while(i-- > 6) {
            Exp arg = it.next();
            it.remove();
            
            // Add string to program if used as argument
            if (arg.isString()) {
                instrList.add(arg);
            }
            
            // Move on stack
            instrList.add(new Push(arg));
        }
        
        
        // 2. Move registers to destination (swap if conflicting src/dest)
        Collections.reverse(argLocs);
        for (int j = 0; j < argLocs.size(); j++) {
            Exp arg = argLocs.get(j);
            if (!arg.isReg()) continue;
            
            // Add string to program if used as argument
            if (arg.isString()) {
                instrList.add(arg);
            }

            // Mark as not conflicting anymore (scrap register)
            argLocs.set(j, Register.r11()); 
            
            // If conflict: move 2nd(conflict) to %r10, move 1st arg to dest, move 2nd to 1st arg reg
            Exp dest = Call.getParamAtIndex(j);
            
            // Move only if needed or if sign extension is needed
            if (!arg.equals(dest)) {
                
                if (argLocs.contains(dest)) {
                    // Preserve if conflict with destination
                    instrList.add(new Mov(dest, Register.r10()));
                    instrList.add(new MovSx(arg, dest));
                    instrList.add(new Mov(Register.r10(), arg));
                    
                    // Update argument list
                    for (int k = 0; k < 6; k++) {
                        // future conflicted registers are moved to current arg location
                        int size = argLocs.get(k).getSize();
                        if (argLocs.get(k).equals(dest)) {
                            argLocs.set(k, new Register((Register) arg, size));
                        } else if (argLocs.get(k).equals(arg)) {
                            argLocs.set(k, new Register((Register) dest, size));
                        }
                    }
                    
                } else {
                    instrList.add(new MovSx(arg, dest));
                }
                
            }   else if (arg.getSize() == 1) {
                // Sign extend if src == dest but src is boolean
                instrList.add(new MovSx(arg, dest));
            }
        }
        
        
        // 3. Move rest of arguments
        for (int j = 0; j < argLocs.size(); j++) {
            Exp arg = argLocs.get(j);
            if (arg.isReg()) continue;
            
            // Add string to program if used as argument
            if (arg.isString()) {
                instrList.add(arg);
            }
            
            Exp dest = Call.getParamAtIndex(j);
            instrList.add(new MovSx(arg, dest));
        }
        
    }
    
    private List<LIR> checkArrayBounds(Exp ind, int lenght) {

        List<LIR> checkInstr = new ArrayList<LIR>();
        ErrorHandle boundErr = ErrorHandle.outOfBounds();
        
        // Check lower bound
        checkInstr.add(new Mov(ind, Register.r10()));
        checkInstr.add(new BinOp("cmp", new Literal(0), Register.r10()));
        checkInstr.add(new Jump(boundErr.getLabel(), BinaryOperator.LT));
        
        // Check upper bound
        checkInstr.add(new Mov(ind, Register.r10()));
        checkInstr.add(new BinOp("cmp", new Literal(lenght), Register.r10()));
        checkInstr.add(new Jump(boundErr.getLabel(), BinaryOperator.GE));
     
        checkInstr.add(boundErr);
        
        return checkInstr;
    }
    
    private List<LIR> handlePlus(Exp dest, Exp lhs, Exp rhs) {

        List<LIR> instrList = new ArrayList<LIR>();
        Register r10 = Register.r10();
        
        // Special cases
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
                    instrList.add(new BinOp("add", rhs, dest));
                } else {
                    instrList.add(new Mov(rhs, r10));
                    instrList.add(new BinOp("add", r10, dest));
                }
            }
        } else if (rhs.equals(dest)) {
            if (lhs.equals(new Literal(1))) {
                // a = 1 + a
                instrList.add(new Inc(dest));
            } else {
                // a = b + a
                if (lhs.isImm() || dest.isImm()) {
                    instrList.add(new BinOp("add", lhs, dest));
                } else {
                    instrList.add(new Mov(lhs, r10));
                    instrList.add(new BinOp("add", r10, dest));
                }
            }
        } else if (lhs.equals(rhs)) {
            // a = b + b;
            if (dest.isReg() || lhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
            } else {
                instrList.add(new Mov(lhs, r10));
                instrList.add(new Mov(r10, dest));
            }
            instrList.add(new LShift(dest));
            
        
        // Destination is register
        } else if (dest.isReg()) {
            
            // Both operands immediate
            if (lhs.isImm() && rhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("add", rhs, dest));
             
            // One operand in memory
            } else if (lhs.isImm()) {
                instrList.add(new Mov(rhs, dest));
                instrList.add(new BinOp("add", lhs, dest));
            } else if (lhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("add", rhs, dest));
                
            // Both operands in memory
            } else {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("add", rhs, dest));
            }
            
                
        // Destination in memory
        } else {
            
            // Make sure the memory is moved in r10 first
            if (lhs.isImm()) {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new BinOp("add", lhs, r10));
                instrList.add(new Mov(r10, dest));
            } else {
                instrList.add(new Mov(lhs, r10));
                instrList.add(new BinOp("add", rhs, r10));
                instrList.add(new Mov(r10, dest));
            }
        }
        return instrList;
    }
    
    private List<LIR> handleMinus(Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        Register r10 = Register.r10();
        
        // Special cases
        if (rhs.equals(new Literal(1))) {
            if (lhs.equals(dest)) {
                // a = a - 1
                instrList.add(new Dec(dest));
            } else {
                // a = b - 1
                if (dest.isReg()) {
                    instrList.add(new Mov(lhs, dest)); 
                    instrList.add(new Dec(dest));
                } else {
                    instrList.add(new Mov(lhs, r10));
                    instrList.add(new Dec(r10));
                    instrList.add(new Mov(r10, dest));
                }                
            }
        } else if (dest.equals(lhs)) {
            // a = a - c
            if (dest.isReg() || rhs.isImm()) {
                instrList.add(new BinOp("sub", rhs, dest)); 
            } else {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new BinOp("sub", r10, dest)); 
            }

            
        // Destination is register
        } else if (dest.isReg()) {   
            if (dest.equals(rhs)) {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("sub", r10, dest));
            } else {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("sub", rhs, dest));
            }
            
            
        // Destination in memory
        } else {   
            instrList.add(new Mov(lhs, r10));
            instrList.add(new BinOp("sub", rhs, r10));
            instrList.add(new Mov(r10, dest));
        }
            
        return instrList;
    }
    
    private List<LIR> handleTimes(Exp dest, Exp lhs, Exp rhs) {

        List<LIR> instrList = new ArrayList<LIR>();
        Register r10 = Register.r10();
        
        // Special cases
        if (isPow2(lhs)) {
            int pow2 = log2(((Literal) lhs).getValue());
            if (rhs.equals(dest)) {
                // a = 2^i * a
                instrList.add(new LShift(dest, pow2));
            } else {
                // a = 2^i * c
                if (dest.isReg() || rhs.isImm()) {
                    instrList.add(new Mov(rhs, dest));
                } else {
                    instrList.add(new Mov(rhs, r10));
                    instrList.add(new Mov(r10, dest));
                }                    
                instrList.add(new LShift(dest, pow2));
            }
                
        } else if (isPow2(rhs)) {
            int pow2 = log2(((Literal) rhs).getValue());
            if (lhs.equals(dest)) {
                // a = a * 2^i
                instrList.add(new LShift(dest, pow2));
            } else {
                // a = b * 2^i
                if (dest.isReg() || lhs.isImm()) {
                    instrList.add(new Mov(lhs, dest));
                } else {
                    instrList.add(new Mov(lhs, r10));
                    instrList.add(new Mov(r10, dest));
                }                    
                instrList.add(new LShift(dest, pow2));
            }
        
        } else if (lhs.equals(dest) && dest.isReg()) {
            // a = a * c
            instrList.add(new BinOp("imul", rhs, dest));
        } else if (rhs.equals(dest) && dest.isReg()) {
            // a = b * a
            instrList.add(new BinOp("imul", lhs, dest));      
   
        
        // Destination is register
        } else if (dest.isReg()) {
            
            // Both operands immediate
            if (lhs.isImm() && rhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("imul", rhs, dest));
             
            // One operand in memory
            } else if (lhs.isImm()) {
                instrList.add(new Mov(rhs, dest));
                instrList.add(new BinOp("imul", lhs, dest));
            } else if (lhs.isImm()) {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("imul", rhs, dest));
                
            // Both operands in memory
            } else {
                instrList.add(new Mov(lhs, dest));
                instrList.add(new BinOp("imul", rhs, dest));
            }
                
            
        // Destination in memory
        } else {            
            instrList.add(new Mov(lhs, r10));
            instrList.add(new BinOp("imul", rhs, r10));
            instrList.add(new Mov(r10, dest));
        }
        
        return instrList;
    }

    private List<LIR> handleDivMod(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        Register r10 = Register.r10();
        Register rax = Register.rax();
        Register rdx = Register.rdx();
        
        // Special cases
        if (isPow2(rhs)) {
            int pow2 = log2(((Literal) rhs).getValue());
            // Move lhs to destination
            if (!dest.equals(lhs)) {
                if (dest.isReg() || lhs.isImm()) {
                    instrList.add(new Mov(lhs, dest));
                } else {
                    instrList.add(new Mov(lhs, r10));
                    instrList.add(new Mov(r10, dest));
                }
            }

            // a = a /|% 2^i
            if (op.equals(BinaryOperator.DIVIDE)) {
                // lsh(a, i)
                instrList.add(new RShift(dest, pow2));   
            } else if (op.equals(BinaryOperator.MOD)) {
                // bitAnd(a, i-1)
                instrList.add(new BinOp("and", new Literal(pow(2, pow2)-1), dest));
            } else {
                throw new Error("Unexpected");
            }
        
        
        // Base case 
        } else {
            // %rax <-- lhs
            if (this.method.isLive(Register.rax()) && !dest.equals(rax)) {
                instrList.add(new Push(rax)); 
            }
            if (rhs.equals(rax)) {
                instrList.add(new Mov(rhs, r10));
            }
            if (!lhs.equals(rax)) {
                instrList.add(new Mov(lhs, rax));
            }
            
            // %rdx <-- 0
            if (this.method.isLive(Register.rdx()) && !dest.equals(rdx)) {
                instrList.add(new Push(rdx)); 
            }
            if (rhs.equals(rdx)) {
                instrList.add(new Mov(rhs, r10));
            }
            instrList.add(new BinOp("xor", rdx, rdx));
            
            // Integer division
            instrList.add(new Command("cqto"));
            if (rhs.equals(rdx) || rhs.equals(rax)) {
                instrList.add(new UnOp("idiv", r10));
            } else {
                instrList.add(new UnOp("idiv", rhs));
            }
            
            // Return result to correct register
            if (op.equals(BinaryOperator.DIVIDE) && !dest.equals(rax)) {
                instrList.add(new Mov(rax, dest));  
            } else if (op.equals(BinaryOperator.MOD) && !dest.equals(rdx)) {
                instrList.add(new Mov(rdx, dest)); 
            }
            
            // Restore %rax, %rdx
            if (this.method.isLive(Register.rdx()) && !dest.equals(rdx)) {
                instrList.add(new Pop(rdx)); 
            }
            if (this.method.isLive(Register.rax()) && !dest.equals(rax)) {
                instrList.add(new Pop(rax)); 
            }
        }        
        
        return instrList;
    }
    
    private List<LIR> handleBool(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        String opStr = (op.equals(BinaryOperator.AND)) ? "and" : "or";
        
        Register r10 = new Register(Register.r10(), 1);
        
        // Special cases
        if (dest.equals(lhs)) {
            // a = a && c
            if (dest.isReg() || rhs.isImm()) {
                instrList.add(new BinOp(opStr, rhs, dest));
            } else {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new BinOp(opStr, r10, dest));
            }
        } else if (dest.equals(rhs)) {
            // a = b && a
            if (dest.isReg() || lhs.isImm()) {
                instrList.add(new BinOp(opStr, lhs, dest));
            } else {
                instrList.add(new Mov(lhs, r10));
                instrList.add(new BinOp(opStr, r10, dest));
            }
        
        
        // Destination in register
        } else if (dest.isReg()) {
            instrList.add(new Mov(lhs, dest));
            instrList.add(new BinOp(opStr, rhs, dest));
        
        
        // Destination in memory     
        } else {
            if (lhs.isImm()) {
                instrList.add(new Mov(lhs, r10));
                instrList.add(new BinOp(opStr, rhs, r10));
                instrList.add(new Mov(r10, dest));
            } else {
                instrList.add(new Mov(rhs, r10));
                instrList.add(new BinOp(opStr, lhs, r10));
                instrList.add(new Mov(r10, dest));
            }
        }
        
        return instrList;
    }
    
    private List<LIR> handleComp(BinaryOperator op, Exp dest, Exp lhs, Exp rhs) {
        List<LIR> instrList = new ArrayList<LIR>();
        
        Register r10 = Register.r10();
        
        // Do comparison
        if (lhs.isLiteral()) {
            instrList.add(new Mov(lhs, r10));
            instrList.add(new BinOp("cmp", rhs, r10));
        } else if (rhs.isImm()) {                
            instrList.add(new BinOp("cmp", rhs, lhs)); 
        } else {
            instrList.add(new Mov(rhs, r10));
            instrList.add(new BinOp("cmp", r10, lhs));
        }
        
        // Eventually store result
        if (dest != null) {
            instrList.add(new Set(op, dest));
        }
        
        return instrList;
    }
    
    private int log2(int value) {
        int res = 1;
        int pow = 0;
        while (res <= value) {
            if (res == value) {
                return pow;
            }
            res *= 2;
            pow++;
        }
        return -1;
    }
    
    private int pow(int base, int exp) {
        int res = 1;
        while(exp-- > 0) {
            res *= base;
        }
        return res;
    }
    
    private boolean isPow2(Exp exp) {
        if (exp.isLiteral()) {
            Literal value = (Literal) exp;
            return log2(value.getValue()) > 0;
        } 
        return false;
    }

}
