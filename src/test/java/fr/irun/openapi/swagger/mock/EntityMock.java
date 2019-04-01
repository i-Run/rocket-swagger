package fr.irun.openapi.swagger.mock;

import java.time.Instant;

public class EntityMock<T> {

    private String id;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    private T entity;

}
