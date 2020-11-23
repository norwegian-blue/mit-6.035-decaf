package ir;

/**
 * @author Nicola
 */
public abstract class Ir {
    
    protected static boolean printAsTree = true;
    
    public static void setTreePrint(boolean asTree) {
        printAsTree = asTree;
    }
    
    private int line_number;
    private int column_number;
    
    public static String indent(String input) {
        return input.replaceAll("(?m)^", "  ");
    }

    public int getLineNum() {
        return line_number;
    }
    
    public int getColNum() {
        return column_number;
    }
    
    public void setLineNum(int line_number) {
        this.line_number = line_number;
    }
    
    public void setColNum(int column_number) {
        this.column_number = column_number;
    }
    
    public abstract <T> T accept(IrVisitor<T> v);
    
    public boolean isClass() {
        return false;
    }
    
    public boolean isExp() {
        return false;
    }
    
    public boolean isStm() {
        return false;
    }
}
