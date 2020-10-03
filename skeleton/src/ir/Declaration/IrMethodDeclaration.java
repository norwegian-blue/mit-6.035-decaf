package ir.Declaration;

import java.util.List;
import ir.Statement.IrBlock;
import semantic.Type;

/**
 * @author Nicola
 */
public class IrMethodDeclaration extends IrMemberDeclaration{

        private final String name;
        private final Type type;
        private final List<IrVariableDeclaration> arguments;
        private final IrBlock body;
        
        public IrMethodDeclaration(String name, Type type, List<IrVariableDeclaration> arguments, IrBlock body) {
            this.name = name;
            this.type = type;
            this.arguments = arguments;
            this.body = body;
        }
        
}
