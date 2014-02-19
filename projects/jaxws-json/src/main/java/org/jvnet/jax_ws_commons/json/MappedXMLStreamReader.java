package org.jvnet.jax_ws_commons.json;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.util.FastStack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import java.util.Set;

public class MappedXMLStreamReader extends AbstractXMLStreamReader {
    private final FastStack nodes;
    private String currentValue;
    private final MappedNamespaceConvention convention;
    private String valueKey = "$";
    private final NamespaceContext ctx;
    private int popArrayNodes;

    public MappedXMLStreamReader(final JSONObject obj)
            throws JSONException, XMLStreamException {
        this(obj, new MappedNamespaceConvention());
    }

    public MappedXMLStreamReader(final JSONObject obj, final MappedNamespaceConvention con)
            throws JSONException, XMLStreamException {
        final String rootName = (String) obj.keys().next();

        this.convention = con;
        this.nodes = new FastStack();
        this.ctx = con;
        final Object top = obj.get(rootName);
        if (top instanceof JSONObject) {
            this.node = new Node(null, rootName, (JSONObject) top, convention);
        } else if (top instanceof JSONArray && !(((JSONArray) top).length() == 1 && "".equals(((JSONArray) top).get(0)))) {
            this.node = new Node(null, rootName, obj, convention);
        } else {
            node = new Node(rootName, convention);
            convention.processAttributesAndNamespaces(node, obj);
            currentValue = JSONObject.NULL.equals(top) ? null : top.toString();
        }
        nodes.push(node);
        event = START_DOCUMENT;
    }


    @Override
    public int next() throws XMLStreamException {
        if (event == START_DOCUMENT) {
            event = START_ELEMENT;
        } else if (event == CHARACTERS) {
            event = END_ELEMENT;
            node = (Node) nodes.pop();
            currentValue = null;
        } else if (event == START_ELEMENT || event == END_ELEMENT) {
            if (event == END_ELEMENT && !nodes.isEmpty()) {
                node = (Node) nodes.peek();
                if (popArrayNodes > 0) {
                    nodes.pop();
                    if (node.getArray() != null) {
                        popArrayNodes--;
                        event = END_ELEMENT;
                        return event;
                    }
                }
            }
            if (currentValue != null) {
                event = CHARACTERS;
            } else if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                processElement();
            } else {
                if (!nodes.isEmpty()) {
                    event = END_ELEMENT;
                    node = (Node) nodes.pop();
                } else {
                    event = END_DOCUMENT;
                }
            }
        }
        // handle value in nodes with attributes
        if (!nodes.isEmpty()) {
            final Node next = (Node) nodes.peek();
            if (event == START_ELEMENT && next.getName().getLocalPart().equals(valueKey)) {
                event = CHARACTERS;
                node = (Node) nodes.pop();
            }
        }
        return event;
    }

    private void processElement() throws XMLStreamException {
        try {
            final Object newObj;
            String nextKey;
            if (node.getArray() != null) {
                int index = node.getArrayIndex();
                if (index >= node.getArray().length()) {
                    nodes.pop();
                    node = (Node) nodes.peek();

                    if (node == null) {
                        event = END_DOCUMENT;
                        return;
                    }

                    if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                        if (popArrayNodes > 0) {
                            node = (Node) nodes.pop();
                        }
                        processElement();
                    } else {
                        event = END_ELEMENT;
                        node = (Node) nodes.pop();
                    }
                    return;
                }
                newObj = node.getArray().get(index++);
                nextKey = node.getName().getLocalPart();
                if (node.getName().getNamespaceURI() != null && !node.getName().getNamespaceURI().isEmpty()) {
                    final String prefix = this.convention.getPrefix(node.getName().getNamespaceURI());
                    if (null != prefix && !prefix.isEmpty()) {
                        nextKey = prefix + "." + nextKey;
                    }
                }
                node.setArrayIndex(index);
            } else {
                nextKey = (String) node.getKeys().next();
                newObj = node.getObject().get(nextKey);
            }
            if (newObj instanceof String) {
                node = new Node(nextKey, convention);
                nodes.push(node);
                currentValue = (String) newObj;
                event = START_ELEMENT;
            } else if (newObj instanceof JSONArray) {
                final JSONArray array = (JSONArray) newObj;
                if (!processUniformArrayIfPossible(nextKey, array)) {
                    node = new Node(nextKey, convention);
                    node.setArray(array);
                    node.setArrayIndex(0);
                    nodes.push(node);
                    processElement();
                }
            } else if (newObj instanceof JSONObject) {
                node = new Node((Node) nodes.peek(), nextKey, (JSONObject) newObj, convention);
                nodes.push(node);
                event = START_ELEMENT;
            } else {
                node = new Node(nextKey, convention);
                nodes.push(node);
                currentValue = JSONObject.NULL.equals(newObj) ? null : newObj.toString();
                event = START_ELEMENT;
            }
        } catch (final JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    private boolean processUniformArrayIfPossible(final String arrayKey, final JSONArray array) throws JSONException, XMLStreamException {
        if (!isAvoidArraySpecificEvents(arrayKey)) {
            return false;
        }

        final int arrayLength = array.length();
        int depth = 0;
        String lastKey = null;
        final int parentIndex = nodes.size();
        final boolean isRoot = ((Node) nodes.get(0)).getName().getLocalPart().equals(arrayKey);
        final Node parent = !isRoot ? new Node(arrayKey, convention) : node;

        for (int i = arrayLength - 1; i >= 0; i--) {
            final Object object = array.get(i);
            if (object instanceof JSONObject) {
                final JSONObject jsonObject = (JSONObject) object;
                // lets limit to single key JSONObjects for now
                if (jsonObject.length() == 1) {
                    final String theKey = jsonObject.keys().next().toString();
                    if (lastKey == null || lastKey.equals(theKey)) {
                        lastKey = theKey;
                        depth++;
                        final Node theNode = new Node(parent, theKey, jsonObject, convention);
                        nodes.push(theNode);
                    } else {
                        lastKey = null;
                        break;
                    }
                }
            }
        }
        if (lastKey == null) {
            for (int i = 0; i < depth; i++) {
                nodes.pop();
            }
            return false;
        }

        parent.setArray(array);
        parent.setArrayIndex(arrayLength);
        if (!isRoot) {
            nodes.add(parentIndex, parent);
            nodes.push(parent);
            node = parent;
            event = START_ELEMENT;
        } else {
            node = (Node) nodes.pop();
            processElement();
        }
        popArrayNodes++;
        return true;
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public String getElementText() throws XMLStreamException {
        event = CHARACTERS;
        return currentValue;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return ctx;
    }

    @Override
    public String getText() {
        if (convention.isReadNullAsString() && currentValue != null
            && "null".equals(currentValue)) {
            return "";
        }
        return currentValue;
    }

    public void setValueKey(final String valueKey) {
        this.valueKey = valueKey;
    }

    public boolean isAvoidArraySpecificEvents(final String key) {
        final Set keys = convention.getPrimitiveArrayKeys();
        return keys != null && keys.contains(key);
    }
}
