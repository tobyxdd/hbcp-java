package com.vecsight.commons.hbcp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HBCPValue {

    private HBCPValueType valueType;

    private String string;

    private byte[] bytes;

    public HBCPValue(String string) {
        this.valueType = HBCPValueType.STRING;
        this.string = string;
    }

    public HBCPValue(byte[] bytes) {
        this.valueType = HBCPValueType.BYTES;
        this.bytes = bytes;
    }

    public String getString() throws ValueTypeMismatchException {
        return getString(StandardCharsets.UTF_8);
    }

    public String getString(Charset charset) throws ValueTypeMismatchException {
        if (valueType == HBCPValueType.BYTES) {
            return new String(bytes, charset);
        } else if (valueType == HBCPValueType.STRING) {
            return string;
        } else {
            throw new ValueTypeMismatchException("Unknown type");
        }
    }

    public byte[] getBytes() throws ValueTypeMismatchException {
        return getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getBytes(Charset charset) throws ValueTypeMismatchException {
        if (valueType == HBCPValueType.STRING) {
            return string.getBytes(charset);
        } else if (valueType == HBCPValueType.BYTES) {
            return bytes;
        } else {
            throw new ValueTypeMismatchException("Unknown type");
        }
    }

    public HBCPValueType getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HBCPValue{");
        sb.append("valueType=").append(valueType);
        sb.append(", string='").append(string).append('\'');
        sb.append(", bytes=");
        if (bytes == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < bytes.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(bytes[i]);
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }
}
