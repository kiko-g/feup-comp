import pt.up.fe.comp.jmm.AttributeType;
import pt.up.fe.comp.jmm.JmmNode;

import java.util.*;

public class SimpleNode implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Jmm parser;
    private final Map<AttributeType, String> attributes;

    public SimpleNode(int i) {
        id = i;
        this.attributes = new HashMap<>();
    }

    public SimpleNode(Jmm p, int i) {
        this(i);
        parser = p;
    }

    public String getKind() {
        return toString();
    }

    public List<String> getAttributes() {
        List<AttributeType> types = new ArrayList<>(this.attributes.keySet());
        List<String> attributes = new ArrayList<>();
        for(AttributeType type : types) {
            attributes.add(type.toString());
        }

        return attributes;
    }

    public void put(AttributeType attribute, String value) {
        this.attributes.put(attribute, value);
    }

    public String get(String attribute) {
        AttributeType type = AttributeType.valueOf(attribute);
        return this.attributes.get(type);
    }

    public List<JmmNode> getChildren() {
        List<JmmNode> childNodes = new ArrayList<>();

        if(this.children == null) {
          return childNodes;
        }

        Collections.addAll(childNodes, this.children);
        return childNodes;
    }

    public int getNumChildren() {
        return jjtGetNumChildren();
    }

    public void add(JmmNode child, int index) {
        jjtAddChild((Node) child, index);
    }

    public void jjtOpen() { }

    public void jjtClose() { }

    public void jjtSetParent(Node n) {
        this.parent = n;
    }

    public Node jjtGetParent() {
        return this.parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (this.children == null) {
            this.children = new Node[i + 1];
        } else if (i >= this.children.length) {
          Node[] c = new Node[i + 1];
          System.arraycopy(this.children, 0, c, 0, this.children.length);
            this.children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return this.children[i];
    }

    public int jjtGetNumChildren() {
        return (this.children == null) ? 0 : this.children.length;
    }

    public String toString() {
        return JmmTreeConstants.jjtNodeName[this.id];
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (this.children != null) {
          for (int i = 0; i < this.children.length; ++i) {
              SimpleNode n = (SimpleNode)this.children[i];
              if (n != null) {
                  n.dump(prefix + " ");
              }
          }
        }
    }

    public int getId() {
        return this.id;
    }
}
