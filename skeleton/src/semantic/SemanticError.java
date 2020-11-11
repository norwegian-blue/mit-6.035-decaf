package semantic;

/**
 * @author Nicola
 */
public class SemanticError {
    
    private final int line;
    private final int col;
    private final String errMsg;

    public static SemanticError NoError = new SemanticError();
    
    public SemanticError() {
        this.line = 0;
        this.col = 0;
        this.errMsg = "";
    }
    
    public SemanticError(int line, int col, String errMsg) {
        this.line = line;
        this.col = col;
        this.errMsg = errMsg;
    }
    
    @Override
    public String toString() {
        if (errMsg.isEmpty()) {
            return "no error";
        } else {
            return "ERROR (line " + String.format("%2d", line) + ", col " + String.format("%2d", col) + "): " + errMsg;
        }
    }
}
