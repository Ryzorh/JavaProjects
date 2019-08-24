package com.mojang.nbt;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */

import java.io.*;

public class LongArrayTag extends Tag {
    public long[] data;

    public LongArrayTag(String name) {
        super(name);
    }

    public LongArrayTag(String name, long[] data) {
        super(name);
        this.data = data;
    }

    void write(DataOutput dos) throws IOException {
        dos.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            dos.writeLong(data[i]);
        }
    }

    void load(DataInput dis) throws IOException {
        int length = dis.readInt();
        data = new long[length];
        for (int i = 0; i < length; i++) {
            data[i] = dis.readLong();
        }
    }

    public byte getId() {
        return TAG_Long_Array;
    }

    public String toString() {
        return "[" + data.length + " bytes]";
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            LongArrayTag o = (LongArrayTag) obj;
            return ((data == null && o.data == null) || (data != null && data.equals(o.data)));
        }
        return false;
    }

    @Override
    public Tag copy() {
        long[] cp = new long[data.length];
        System.arraycopy(data, 0, cp, 0, data.length);
        return new LongArrayTag(getName(), cp);
    }
}
