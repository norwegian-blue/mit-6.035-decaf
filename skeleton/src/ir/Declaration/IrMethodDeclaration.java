package ir.Declaration;

import java.util.List;

import ir.Ir;
import ir.IrVisitor;
import ir.Statement.IrBlock;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public class IrMethodDeclaration extends IrMemberDeclaration{

    private final List<IrParameterDeclaration> methodParams;
    private final IrBlock methodBody;
        
    public IrMethodDeclaration(String methodName, TypeDescriptor methodType, List<IrParameterDeclaration> methodParams, IrBlock methodBody) {
        super(methodType, methodName);
        this.methodParams = methodParams;
        this.methodBody = methodBody;
    }
    
    public IrBlock getBody() {
        return methodBody;
    }
    
    public List<IrParameterDeclaration> getParameters() {
        return methodParams;
    }
     
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        
        String str;
        str = "METHOD " + super.toString() + "(";
        for (IrParameterDeclaration methodArg : this.methodParams) {
            str += "\n" + Ir.indent(methodArg.toString());
        }
        str += "\n)\n" + Ir.indent(methodBody.toString());
        return str;
    }
    
    private String inLineStr() {
        String str;
        str = "METHOD " + super.toString() + "(";
        String args = "";
        if (this.methodParams.size() > 0) {
            for (IrParameterDeclaration methodArg : this.methodParams) {
                args += methodArg.toString() + ", ";
            }
            args = args.substring(0, args.length()-2);
        }
        String bodyStr = methodBody.toString();
        if (bodyStr.length() > 3) {
            bodyStr = bodyStr.substring(2, bodyStr.length()-2);
        }
        str += args + ")\n" + Ir.indent(bodyStr);
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
