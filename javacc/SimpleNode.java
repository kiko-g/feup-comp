import pt.up.fe.comp.jmm.JmmNode;

import java.lang.RuntimeException;
import java.util.*;

public class SimpleNode implements Node {
  protected Node parent;
  protected Node[] children;
  protected int id;
  private final Map<String, String> attributes;

  public SimpleNode(int i) {
      id = i;
      this.attributes = new HashMap<String, String>();
  }

  public String getKind() {
      return toString();
  }

  public List<String> getAttributes() {
      return new ArrayList<>(this.attributes.keySet());
  }

  public void put(String attribute, String value) {
      this.attributes.put(attribute, value);
  }

  public String get(String attribute) {
      return this.attributes.get(attribute);
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
      parent = n;
  }

  public Node jjtGetParent() {
      return parent;
  }

  public void jjtAddChild(Node n, int i) {
      if (children == null) {
          children = new Node[i + 1];
      } else if (i >= children.length) {
          Node c[] = new Node[i + 1];
          System.arraycopy(children, 0, c, 0, children.length);
          children = c;
      }
      children[i] = n;
  }

  public Node jjtGetChild(int i) {
      return children[i];
  }

  public int jjtGetNumChildren() {
      return (children == null) ? 0 : children.length;
  }

  public String toString() {
      return JmmTreeConstants.jjtNodeName[id];
  }

  public String toString(String prefix) {
      return prefix + toString();
  }

  public void dump(String prefix) {
      System.out.println(toString(prefix));
      if (children != null) {
          for (int i = 0; i < children.length; ++i) {
              SimpleNode n = (SimpleNode)children[i];
              if (n != null) {
                  n.dump(prefix + " ");
              }
          }
      }
  }

  public int getId() {
    return id;
  }
}
