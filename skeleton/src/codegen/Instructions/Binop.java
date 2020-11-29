package codegen.Instructions;

import ir.Expression.IrBinaryExpression.*;

/**
 * @author Nicola
 */

public class Binop extends LIR {
    
    private Exp lhs;
    private Exp rhs;
    private BinaryOperator operator;
        
    public Binop(BinaryOperator operator, Exp lhs, Exp rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        
    }
   
    @Override
    public String toCode() {
        String tmp = "";
        switch (this.operator) {
        case PLUS:           
            return "\tadd\t" + lhs.toCode() + ", " + rhs.toCode();
        case MINUS:
            tmp = "\tsub\t" + rhs.toCode() + ", " + lhs.toCode();
            tmp += "\n" + new Mov(lhs, rhs).toCode();
            return tmp;
        case TIMES:
            return "\timul\t" + lhs.toCode() + ", " + rhs.toCode();
        case DIVIDE:
            tmp += new Mov(new Literal(0), new Register(Register.Registers.rdx)).toCode();
            tmp += "\n" + new Mov(lhs, new Register(Register.Registers.rax)).toCode();
            tmp += "\n\tidiv\t" + rhs.toCode();
            tmp += "\n" + new Mov(new Register(Register.Registers.rax), rhs).toCode();
            return tmp;
        case MOD:
            return "";
        case AND:
            return "\tand\t" + lhs.toCode() + ", " + rhs.toCode();
        case OR:
            return "\tor\t" + lhs.toCode() + ", " + rhs.toCode();
        case GT:
        case LT:
        case GE:
        case LE:
        case EQ:
        case NEQ:
        default:
            throw new Error("Unrecognized operation");
        }
            
    }

}
