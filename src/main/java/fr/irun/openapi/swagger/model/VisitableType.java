package fr.irun.openapi.swagger.model;

import java.lang.reflect.Type;

/**
 * Interface for a wrapper allowing to visit types.
 */
public interface VisitableType {

    /**
     * Specification of Type visiting methods.
     *
     * @param <T> Type returned by the visitor.
     */
    interface Visitor<T> {

        T visitDateType(Type dateType);

        T visitResponseEntityType(Type responseEntityType);

        T visitAnyOtherType(Type baseType);
    }

    /**
     * Visit this instance.
     *
     * @param visitor Visitor.
     * @param <T>     Type returned by the visit.
     * @return Visit result.
     */
    <T> T visit(Visitor<T> visitor);

}
