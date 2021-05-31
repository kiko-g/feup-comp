package pt.up.fe.comp.jmm.analysis.table;

public class Type {
    private final String name;
    private final boolean isArray;

    public Type(String name, boolean isArray) {
        this.name = name;
        this.isArray = isArray;
    }

    public String getName() {
        return name;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public String toString() {
        return "Type [name=" + name + ", isArray=" + isArray + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isArray ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Type other = (Type) obj;
        if (isArray != other.isArray)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public String print() {
        var builder = new StringBuilder();

        builder.append(getName());
        if (isArray) {
            builder.append("[]");
        }

        return builder.toString();
    }

}
