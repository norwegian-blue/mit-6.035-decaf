package codegen.Instructions;

/**
 * @author Nicola
 */
public class RShift extends LIR {
    
    private Exp exp;
    private Integer i;
    
    public RShift(Exp exp) {
        this.exp = exp;
        this.i = null;
    }
    
    public RShift(Exp exp, int i) {
        this.exp = exp;
        this.i = i;
    }

    @Override
    public String toCode() {
        String str =  "\tshr" + exp.getSuffix() + "\t";
        if (i != null) {
            str += new Literal(i) + ", ";
        }
        return str + exp.toCode(); 
    }

}