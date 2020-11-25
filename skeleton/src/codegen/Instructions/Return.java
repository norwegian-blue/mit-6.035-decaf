package codegen.Instructions;

/**
 * @author Nicola
 */
public class Return extends LIR {

    @Override
    public String toCode() {
        return "ret";
    }

}
