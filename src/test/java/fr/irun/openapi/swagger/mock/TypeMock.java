package fr.irun.openapi.swagger.mock;

import java.lang.reflect.Type;

public class TypeMock implements Type {

    private final String typeName;

    public TypeMock(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
