package codegen.Instructions;

/**
 * @author Nicola
 */
public class LShift extends LIR {
    
    private Exp exp;
    private Integer i;
    
    public LShift(Exp exp) {
        this.exp = exp;
        this.i = null;
    }
    
    public LShift(Exp exp, int i) {
        this.exp = exp;
        this.i = i;
    }

    @Override
    public String toCode() {
        String str =  "\tshl" + exp.getSuffix() + "\t";
        if (i != null) {
            str += new Literal(i).toCode() + ", ";
        }
        return str + exp.toCode(); 
    }

}
    