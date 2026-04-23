package context;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {
    private final Map<String,Object> scncontext=new HashMap<>();

    public void set(String key,Object value){
        scncontext.put(key,value);
    }
    public <T> T get(String key){
        return (T) scncontext.get(key);
    }
    public void clear(){
        scncontext.clear();
    }



}
