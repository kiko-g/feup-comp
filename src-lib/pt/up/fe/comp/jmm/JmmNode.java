package pt.up.fe.comp.jmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.JmmSerializer;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * This interface represents a node in the Jmm AST.
 * 
 * @author COMP2021
 *
 */
public interface JmmNode {

    /**
     * @return the kind of this node (e.g. MethodDeclaration, ClassDeclaration, etc.)
     */
    String getKind();

    /**
     * @return the names of the attributes supported by this Node kind
     */
    List<String> getAttributes();

    /**
     * Sets the value of an attribute.
     * 
     * @param attribute
     * @param value
     */
    void put(String attribute, String value);

    /**
     * 
     * @param attribute
     * @returns the value of an attribute. To see all the attributes iterate the list provided by
     *          {@link JmmNode#getAttributes()}
     */
    String get(String attribute);

    /**
     * 
     * @param attribute
     * @return the value of the attribute wrapper around an Optional, or Optional.empty() if there is no value for the
     *         given attribute
     */
    default Optional<String> getOptional(String attribute) {
        throw new RuntimeException("Not implemented for this class: " + getClass());
    }

    /**
     * 
     * @return the parent of the current node, or null if this is the root node
     */
    default JmmNode getParent() {
        throw new RuntimeException("Not implemented for this class: " + getClass());
    }

    /**
     * 
     * @param kind
     * @return the first ancestor of the given kind, or Optional.empty() if no ancestor of that kind was found
     */
    default Optional<JmmNode> getAncestor(String kind) {
        var currentParent = getParent();
        while (currentParent != null) {
            if (currentParent.getKind().equals(kind)) {
                return Optional.of(currentParent);
            }

            currentParent = currentParent.getParent();
        }

        return Optional.empty();
    }

    /**
     * 
     * @return the children of the node or an empty list if there are no children
     * 
     */
    List<JmmNode> getChildren();

    /**
     * 
     * @return the number of children of the node
     */
    int getNumChildren();

    /**
     * Adds a new node at the end of the children list
     * 
     * @param child
     */
    default void add(JmmNode child) {
        add(child, getNumChildren());
    }

    /**
     * Inserts a node at the given position
     * 
     * @param child
     * @param index
     */
    void add(JmmNode child, int index);

    default String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(JmmNode.class, new JmmSerializer())
                .create();
        return gson.toJson(this, JmmNode.class);
    }

    static JmmNode fromJson(String json) {
        return JmmNodeImpl.fromJson(json);
    }

    default JmmNode sanitize() {
        return fromJson(this.toJson());
    }

    static <T> List<JmmNode> convertChildren(T[] children) {
        if (children == null) {
            return new ArrayList<>();
        }

        JmmNode[] jmmChildren = SpecsCollections.convert(children, new JmmNode[children.length],
                child -> (JmmNode) child);

        return Arrays.asList(jmmChildren);
    }
}
