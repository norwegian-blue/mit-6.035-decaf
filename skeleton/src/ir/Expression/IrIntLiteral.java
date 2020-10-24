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
            return Integer.parseInt(this.value.substring(2), 16);   // HEX
        } else if (this.value.matches("^\\d+")) {
            return Integer.valueOf(this.value);                     // DEC
        } else { 
            return Integer.valueOf(this.value.charAt(0));           // ASCII
        }
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
