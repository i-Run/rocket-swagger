## Description du repository

Ce repository contient le module Maven utilisé comme dépendance dans celui générant la documentation OpenAPI.

## Bibliothèque Swagger

- [Site officiel de Swagger](https://swagger.io/)
- [Swagger Maven plugin](https://github.com/kongchen/swagger-maven-plugin)

## Module Maven

Module **swagger**: inclus les classes Java et ressources nécessaires au plugin swagger pour générer la documentation OpenAPI.

## Plugin Maven et point d'entrée

La classe `fr.irun.openapi.swagger.RocketModelConverter` est le point d'entrée du plugin Maven.
Elle permet de customiser les modèles générés pour la documentation des API REST.

Le plugin Maven est utilisé dans `unchained` (sous-module `rocket-javadoc`).

