package di;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return;
        }
        instantiateBeans();
    }

    private void parseXml(String xmlPath) throws ParserConfigurationException, IOException, SAXException {
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

    private Bean parseBean(Node bean) throws InvalidPropertiesFormatException {
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

    private Property parseProperty(Node property) throws InvalidPropertiesFormatException {
        NamedNodeMap attributes = property.getAttributes();
        String name = attributes.getNamedItem("name").getNodeValue();
        Node value = attributes.getNamedItem("val");
        if (value != null) {
            return new Property(name, value.getNodeValue(), ValueType.VALUE);
        } else {
            Node ref = attributes.getNamedItem("ref");
            if (ref != null) {
                return new Property(name, ref.getNodeValue(), ValueType.REF);
            } else {
                throw new InvalidPropertiesFormatException("There is no property with val or ref tag: " + name);
            }
        }
    }

    private void instantiateBeans() {

    }

    public <T> T getBean(String beanId, Class<T> beanClass) {
        return (T) objectsById.get(beanId);
    }
}
