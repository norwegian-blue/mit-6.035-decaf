package codegen.Instructions;

/**
 * @author Nicola
 */
public class MainDirective extends LIR {

    @Override
    public String toCode() {
        return "\t.globl main";
    }

}
