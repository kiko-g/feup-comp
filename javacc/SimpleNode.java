import pt.up.fe.comp.jmm.JmmNode;

import java.lang.RuntimeException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class SimpleNode implements Node, JmmNode {
  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Jmm parser;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(Jmm p, int i) {
    this(i);
    parser = p;
  }

  public String getKind() {
    return toString();
  }

  public List<String> getAttributes() {
    throw new RuntimeException("Not implemented yet");
  }

  public void put(String attribute, String value) {
    throw new RuntimeException("Not implemented yet");
  }

  public String get(String attribute) {
    throw new RuntimeException("Not implemented yet");
  }

  public List<JmmNode> getChildren() {
    return (children == null) ? new ArrayList<>() : Arrays.asList((JmmNode[])children);
  }

  public int getNumChildren() {
    return jjtGetNumChildren();
  }

  public void add(JmmNode child, int index) {
    if(!(child instanceof Node)) {
      throw new RuntimeException("Node not supported: " + child.getClass());
    }

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

  public void jjtSetValue(Object value) {
    this.value = value;
  }

  public Object jjtGetValue() {
    return value;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
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
