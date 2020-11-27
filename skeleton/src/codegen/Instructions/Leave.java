package codegen.Instructions;

/** 
 * @author Nicola
 */
public class Leave extends LIR {

    @Override
    public String toCode() {
        return "\tleave";
    }

}
