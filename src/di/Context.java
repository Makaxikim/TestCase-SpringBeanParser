package di;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private Map<String, Object> objectsById;
    private List<Bean> beans;

    public Context(String xmlPath) {
        objectsById = new HashMap<>();
        beans = new ArrayList<>();
        parseXml(xmlPath);
        instantiateBeans();
    }

    private void parseXml(String xmlPath) {

    }

    private void instantiateBeans() {

    }

    public <T> T getBean(String beanId, Class<T> beanClass) {
        return (T) objectsById.get(beanId);
    }
}
