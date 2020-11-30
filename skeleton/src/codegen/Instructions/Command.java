package codegen.Instructions;

/**
 * @author Nicola
 */
public class Command extends LIR {

    private String command;
    
    public Command(String command) {
        this.command = command;
    }
    
    @Override
    public String toCode() {
        return command;
    }   
}
