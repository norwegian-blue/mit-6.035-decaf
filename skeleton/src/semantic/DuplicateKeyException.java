package semantic;

/**
 * @author Nicola
 */
public class DuplicateKeyException extends Exception {
    
    static final long serialVersionUID = 0L;
    
    public DuplicateKeyException(String errorMsg) {
        super(errorMsg);
    }

}
