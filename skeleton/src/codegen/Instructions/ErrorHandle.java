package codegen.Instructions;

public class ErrorHandle extends LIR {

    private int errCode;
    private String errMessage;
    
    private ErrorHandle(String errMessage, int errCode) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }
    
    public static ErrorHandle outOfBounds() {
        return new ErrorHandle("Array out of Bounds access", 255);
    }
    
    public static ErrorHandle fallOver() {
        return new ErrorHandle("Control fell over method returning value", 254);
    }
    
    public String getLabel() {
        return "errHandler_" + errCode;
    }
    
    public StringLiteral getErrorMsg() {
        return new StringLiteral("*** RUNTIME ERROR ***: " + this.errMessage + "\\n", "_err_msg_" + errCode); 
    }

    @Override
    public String toCode() {
        String handler = new Label(this.getLabel()).toCode();
        handler += "\n" + new Mov(this.getErrorMsg(), Register.rdi()).toCode();
        handler += "\n\tmovq\t$0, %rax";
        handler += "\n\tcall\tprintf";
        handler += "\n\tmov\t$" + this.errCode + ", %rdi";
        handler += "\n\tcall\texit";
        return handler;
    }
    
    @Override
    public boolean isErrorHandler() {
        return true;
    }
    
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof ErrorHandle)) {
            return false;
        }
        ErrorHandle thatErr = (ErrorHandle)that;
        return this.errCode == thatErr.errCode;
    }
    
    @Override
    public int hashCode() {
        return errCode;
    }
    
}