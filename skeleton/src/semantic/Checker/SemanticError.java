package semantic.Checker;

/**
 * @author Nicola
 */
public class SemanticError {
    
    private final int line;
    private final int col;
    private final String errMsg;
    private final String errType;

    public static SemanticError NoError = new SemanticError();
    
    public SemanticError() {
        this.line = 0;
        this.col = 0;
        this.errMsg = "";
        this.errType = "";
    }
    
    public SemanticError(int line, int col, String errType, String errMsg) {
        this.line = line;
        this.col = col;
        this.errMsg = errMsg;
        this.errType = errType;
    }
    
    @Override
    public String toString() {
        if (errMsg.isEmpty()) {
            return "no error";
        } else {
            return "ERROR (line " + line + ", col " + col + ") [" + errType + "] :\t" + errMsg;
        }
    }
}
