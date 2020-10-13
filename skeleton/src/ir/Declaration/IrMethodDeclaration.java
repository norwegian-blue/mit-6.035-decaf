package ir.Declaration;

import java.util.List;

import ir.Ir;
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
     
    @Override
    public String toString() {
        String str;
        str = "METHOD " + super.toString() + "(";
        for (IrParameterDeclaration methodArg : this.methodParams) {
            str += "\n" + Ir.indent(methodArg.toString());
        }
        str += "\n)\n" + Ir.indent(methodBody.toString());
        return str;
    }
}
