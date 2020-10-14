package semantic;

public class Bucket {
    
    String key;
    Descriptor binding;
    Bucket next;
    
    public Bucket(String key, Descriptor binding, Bucket next){
        this.key = key;
        this.binding = binding;
        this.next = next;
    }
    
    public Descriptor getBinding() {
        return this.binding;
    }
    
    public Bucket getNext() {
        return this.next;
    }  
    
    public boolean hasNext() {
        return this.next == null;
    }
}
