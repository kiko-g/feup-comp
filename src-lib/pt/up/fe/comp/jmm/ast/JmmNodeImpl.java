package pt.up.fe.comp.jmm.ast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

public class JmmNodeImpl implements JmmNode {

    protected String kind;
    protected Map<String, String> attributes;
    protected List<JmmNode> children;
    private JmmNode parent;

    public JmmNodeImpl(String kind) {
        this.kind = kind;
        this.children = new ArrayList<>();
        this.attributes = new LinkedHashMap<>();
    }

    @Override
    public JmmNode getParent() {
        return this.parent;
    }

    @Override
    public List<JmmNode> getChildren() {
        return this.children;
    }

    @Override
    public String getKind() {
        return this.kind;
    }

    @Override
    public List<String> getAttributes() {
        return new ArrayList<>(this.attributes.keySet());
    }

    @Override
    public String get(String attribute) {
        var value = this.attributes.get(attribute);

        SpecsCheck.checkNotNull(value, () -> "Node " + toString() + " does not contain attribute '" + attribute + "'");

        return value;
    }

    @Override
    public Optional<String> getOptional(String attribute) {
        return Optional.ofNullable(this.attributes.get(attribute));
    }

    @Override
    public int getNumChildren() {
        return this.children.size();
    }

    @Override
    public void put(String attribute, String value) {
        this.attributes.put(attribute, value);
    }

    @Override
    public void add(JmmNode child) {
        if (!(child instanceof JmmNodeImpl)) {
            throw new RuntimeException(
                    getClass().getName() + " can only have children of his class (" + getClass().getName() + ").");
        }
        add((JmmNodeImpl) child);
    }

    public void add(JmmNodeImpl child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void add(JmmNode child, int index) {
        if (!(child instanceof JmmNodeImpl)) {
            throw new RuntimeException(
                    getClass().getName() + " can only have children of his class (" + getClass().getName() + ").");
        }
        add((JmmNodeImpl) child, index);
    }

    public void add(JmmNodeImpl child, int index) {
        this.children.add(index, child);
        child.setParent(this);
    }

    /**
     * Convert the string into a JmmNode instance
     * 
     * @param <N>
     * @param source
     * @param nodeClass
     * @return
     */
    public static JmmNodeImpl fromJson(String source) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JmmNode.class, new JmmDeserializer())
                .registerTypeAdapter(JmmNodeImpl.class, new JmmDeserializer())
                // .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.fromJson(source, JmmNodeImpl.class);
    }

    public void setParent(JmmNodeImpl parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        var attrs = getAttributes();
        var attrsString = attrs.isEmpty() ? ""
                : attrs.stream()
                        .map(attr -> attr + ": " + get(attr))
                        .collect(Collectors.joining(", ", " (", ")"));
        return getKind() + attrsString;
    }

    @Override
    public JmmNode removeChild(int index) {
        int numChildren = children.size();
        if (index >= numChildren) {
            System.out.println(
                    "[WARNING] Tried to remove child at index " + index + ", but node only has " + numChildren
                            + " children");
            return null;
        }

        var removedChild = (JmmNodeImpl) children.remove(index);
        removedChild.parent = null;
        return removedChild;
    }

    @Override
    public int removeChild(JmmNode node) {
        // Find index of node
        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);

            // Test if same node
            if (node != child) {
                continue;
            }

            // Found node
            removeChild(i);
            return i;
        }

        System.out
                .println("[WARNING] Tried to remove child from node, but could not find it.\nChild:" + node
                        + "\nParent:" + this);
        return -1;
    }

    @Override
    public void delete() {
        var parent = getParent();
        if (parent == null) {
            System.out.println("[WARNING] Tried to remove itself from the tree, but node has no parent");
            return;
        }

        parent.removeChild(this);
    }
}
