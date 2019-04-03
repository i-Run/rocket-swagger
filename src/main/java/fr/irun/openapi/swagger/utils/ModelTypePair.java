package fr.irun.openapi.swagger.utils;

import lombok.Builder;

import java.lang.reflect.Type;

@Builder
public class ModelTypePair {

    public final Type type;
    public final ModelEnum model;

}
