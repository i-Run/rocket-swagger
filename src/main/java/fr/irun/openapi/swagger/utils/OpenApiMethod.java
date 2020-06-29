package fr.irun.openapi.swagger.utils;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.http.HttpMethod;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum OpenApiMethod {
    GET(HttpMethod.GET.name(), PathItem::get, PathItem::getGet),
    PUT(HttpMethod.PUT.name(), PathItem::put, PathItem::getPut),
    POST(HttpMethod.POST.name(), PathItem::post, PathItem::getPost),
    DELETE(HttpMethod.DELETE.name(), PathItem::delete, PathItem::getDelete),
    TRACE(HttpMethod.TRACE.name(), PathItem::trace, PathItem::getTrace),
    PATCH(HttpMethod.PATCH.name(), PathItem::patch, PathItem::getPatch),
    OPTIONS(HttpMethod.OPTIONS.name(), PathItem::options, PathItem::getOptions),
    HEAD(HttpMethod.HEAD.name(), PathItem::head, PathItem::getHead);

    public final String name;
    public final BiFunction<PathItem, Operation, PathItem> pathItemSetter;
    public final Function<PathItem, Operation> pathItemGetter;

    private static final OpenApiMethod[] VALUES = values();

    OpenApiMethod(String name,
                  BiFunction<PathItem, Operation, PathItem> pathItemSetter, Function<PathItem, Operation> pathItemGetter) {
        this.name = name;
        this.pathItemSetter = pathItemSetter;
        this.pathItemGetter = pathItemGetter;

    }

    public static OpenApiMethod fromName(String name) {
        for (OpenApiMethod value : VALUES) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new NoSuchElementException("OpenApiMethod: " + name);
    }
}
