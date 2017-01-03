package com.chancesnow.jedux;

public class Action<T extends Enum, V> {

    public final T type;
    public final V value;

    public Action(T type) {
        this(type, null);
    }
    public Action(T type, V value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        if (this.value != null) {
            return this.type.toString() + ": " + this.value.toString();
        } else {
            return this.type.toString();
        }
    }
}
