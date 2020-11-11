package semantic;

public class Bucket<T> {
    
    String key;
    T binding;
    Bucket<T> next;
    
    public Bucket(String key, T binding, Bucket<T> next){
        this.key = key;
        this.binding = binding;
        this.next = next;
    }
    
    public T getBinding() {
        return this.binding;
    }
    
    public Bucket<T> getNext() {
        return this.next;
    }  
    
    public boolean hasNext() {
        return this.next != null;
    }
}
