package fr.irun.openapi.swagger.mock;

import java.util.List;

public class NestedMock<T> {

    T element;

    List<NestedMock<T>> children;

    NestedMock<T>[] childrenArray;

}
