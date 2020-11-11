package semantic;

/**
 * @author Nicola
 */
public class KeyNotFoundException extends Exception {
    
    static final long serialVersionUID = 0L;
    
    public KeyNotFoundException(String errorMsg) {
        super(errorMsg);
    }
}
