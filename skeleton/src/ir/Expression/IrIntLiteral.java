package ir.Expression;

import ir.IrVisitor;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */
public class IrIntLiteral extends IrLiteral {
    
    public IrIntLiteral(String value) {
        super(value);
        this.setExpType(BaseTypeDescriptor.INT);
    }
    
    // Return integer literal value
    // Checks if decimal number / hex / ASCII char
    public int eval() {
        if (this.value.matches("^0x.*")) {
            return Integer.parseUnsignedInt(this.value.substring(2), 16);   // HEX
        } else if (this.value.matches("^-?\\d+")) {
            return Integer.valueOf(this.value);                             // DEC
        } else { 
            return Integer.valueOf(this.value.charAt(0));                   // ASCII
        }
    }
    
    // Negate
    public void negate() {
        if (this.value.matches(Long.toString((long)Integer.MAX_VALUE + 1))) {
            this.value = "-" + this.value;
        } else {
            this.value = Integer.toString(-this.eval());
        }            
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
    @Override
    public expKind getExpKind() {
        return IrExpression.expKind.INT;
    }
}
