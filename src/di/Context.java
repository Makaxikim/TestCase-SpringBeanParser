package di;

import di.exceptions.InvalidConfigurationException;
import di.xml.model.Bean;
import di.xml.model.Property;
import di.xml.model.ValueType;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Context {
    private static final String TAG_BEAN = "bean";
    private static final String TAG_PROPERTY = "property";

    private Map<String, Object> objectsById;
    private List<Bean> beans;

    public Context(String xmlPath) {
        objectsById = new HashMap<>();
        beans = new ArrayList<>();
        try {
            parseXml(xmlPath);
        } catch (ParserConfigurationException | IOException | SAXException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }
        try {
            instantiateBeans();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchFieldException
                | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void parseXml(String xmlPath) throws ParserConfigurationException, IOException, SAXException,
            InvalidConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlPath));
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node beanNode = nodes.item(i);
            if (TAG_BEAN.equals(beanNode.getNodeName())) {
                Bean bean = parseBean(beanNode);
                beans.add(bean);
            }
        }
    }

    private Bean parseBean(Node bean) throws InvalidConfigurationException {
        NamedNodeMap attributes = bean.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String classVal = attributes.getNamedItem("class").getNodeValue();
        NodeList beanChildNodes = bean.getChildNodes();
        Map<String, Property> properties = new HashMap<>(beanChildNodes.getLength());
        for (int i = 0; i < beanChildNodes.getLength(); i++) {
            Node propertyNode = beanChildNodes.item(i);
            if (TAG_PROPERTY.equals(propertyNode.getNodeName())) {
                Property property = parseProperty(propertyNode);
                properties.put(property.getName(), property);
            }
        }
        return new Bean(id, classVal, properties);
    }

    private Property parseProperty(Node property) throws InvalidConfigurationException {
        NamedNodeMap attributes = property.getAttributes();
        String name = attributes.getNamedItem("name").getNodeValue();
        Node value = attributes.getNamedItem("value");
        if (value != null) {
            return new Property(name, value.getNodeValue(), ValueType.VALUE);
        } else {
            Node ref = attributes.getNamedItem("ref");
            if (ref != null) {
                return new Property(name, ref.getNodeValue(), ValueType.REF);
            } else {
                throw new InvalidConfigurationException("There is no property with val or ref tag: " + name);
            }
        }
    }

    private void instantiateBeans() throws IllegalAccessException, InstantiationException, ClassNotFoundException,
            NoSuchFieldException, InvalidConfigurationException {
        for (Bean bean : beans) {
            Class<?> aClass = Class.forName(bean.getClassName());
            Object instance = aClass.newInstance();
            for (Property property : bean.getProperties().values()) {
                Field field = getField(aClass, property.getName());
                if (field == null) {
                    throw new NoSuchFieldException();
                }
                field.setAccessible(true);
                switch (property.getType()) {
                    case REF:
                        String refName = property.getValue();
                        if (objectsById.containsKey(refName)) {
                            field.set(instance, objectsById.get(refName));
                            break;
                        } else {
                            throw new InvalidConfigurationException("Failed to instantiate bean");
                        }
                    case VALUE:
                        field.set(instance, convert(field.getType().getName(), property.getValue()));
                        break;
                    default:
                        throw new InvalidConfigurationException("There is no such value type");
                }
            }
            objectsById.put(bean.getId(), instance);
        }
    }

    private Object convert(String typeName, String value) throws InvalidConfigurationException {
        switch (typeName) {
            case "byte":
            case "java.lang.Byte":
                return Byte.valueOf(value);
            case "short":
            case "java.lang.Short":
                return Short.valueOf(value);
            case "int":
            case "java.lang.Integer":
                return Integer.valueOf(value);
            case "long":
            case "java.lang.Long":
                return Long.valueOf(value);
            case "double":
            case "java.lang.Double":
                return Double.valueOf(value);
            case "float":
            case "java.lang.Float":
                return Float.valueOf(value);
            case "boolean":
            case "java.lang.Boolean":
                return Boolean.valueOf(value);
            case "char":
            case "java.lang.Character":
                return value.charAt(0);
            case "java.lang.String":
                return value;
            default:
                throw new InvalidConfigurationException("There is no such type " + typeName);
        }
    }

    private Field getField(Class<?> aClass, String fieldName) throws NoSuchFieldException {
        try {
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = aClass.getSuperclass();
            if (superclass == null) {
                throw e;
            } else {
                return getField(superclass, fieldName);
            }
        }
    }

    public <T> T getBean(String beanId, Class<T> beanClass) {
        return (T) objectsById.get(beanId);
    }
}
