package fr.irun.openapi.swagger.model;

import java.lang.reflect.Type;

/**
 * Abstract implementation of a wrapper for a type.
 */
public abstract class AbstractVisitableType {

    /**
     * Specification of Type visiting methods.
     *
     * @param <T> Type returned by the visitor.
     */
    public interface Visitor<T> {

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
    public abstract <T> T visit(Visitor<T> visitor);

}
