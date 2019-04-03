package fr.irun.openapi.swagger.mock;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeMock implements ParameterizedType {

    private String typeName;

    private Type[] innerTypes;

    public ParameterizedTypeMock(String typeName, Type... innerTypes) {
        this.typeName = typeName;
        this.innerTypes = innerTypes;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return innerTypes;
    }

    @Override
    public Type getRawType() {
        return null;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
