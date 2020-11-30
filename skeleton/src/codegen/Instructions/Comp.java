package codegen.Instructions;

/**
 * @author Nicola
 */
public class Comp extends LIR {

   private Exp lhs;
   private Exp rhs;
   
   public Comp(Exp lhs, Exp rhs) {
       this.lhs = lhs;
       this.rhs = rhs;
   }
   
   @Override
   public String toCode() {
       return "\tcmp\t" + lhs.toCode() + ", " + rhs.toCode();
   }

}
