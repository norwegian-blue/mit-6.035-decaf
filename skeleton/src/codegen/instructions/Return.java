package codegen.instructions;

/**
 * @author Nicola
 */
public class Return extends LIR {

    @Override
    public String toCode() {
        return "ret";
    }

}
