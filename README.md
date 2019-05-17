## Description du repository

Ce repository contient le module Maven utilisé comme dépendance dans celui générant la documentation OpenAPI.

## Bibliothèque Swagger

- [Site officiel de Swagger](https://swagger.io/)
- [Swagger Maven plugin](https://github.com/kongchen/swagger-maven-plugin)
- [Repo git de swagger-ui](https://github.com/swagger-api/swagger-ui)

## Module Maven

Module **swagger**: inclus les classes Java et ressources nécessaires au plugin swagger pour générer la documentation OpenAPI.

## Plugin Maven et point d'entrée

La classe `fr.irun.openapi.swagger.RocketModelConverter` est le point d'entrée du plugin Maven.
Elle permet de customiser les modèles générés pour la documentation des API REST.

Le plugin Maven est utilisé dans `unchained` (sous-module `rocket-javadoc`) pour générer la doc API à partir des controlleurs REST de rocket.

## Servir un fichier JSON avec swagger-ui

Après génération d'un fichier JSON (e.g. à partir de `rocket-javadoc`), il est possible de lancer un docker pour servir la doc API correspondante.

### Installation du docker

- Créer l'arborescence où déposer le fichier

```
sudo mkdir -p /mnt/swagger
sudo chown $USER:$USER /mnt/swagger
mkdir -p /mnt/swagger/api
```

- Afficher un fichier JSON généré avec swagger-ui

```docker pull swaggerapi/swagger-ui```

### Lancement du docker

- Copier le fichier JSON dans l'arborescence

```cp (...)/swagger.json /mnt/swagger/api/```

- Lancer le docker

```docker run -p 80:8080 -e SWAGGER_JSON=/api/swagger.json -v /mnt/swagger/api:/api swaggerapi/swagger-ui```

- Afficher le résultat en se connectant à l'URL `http://localhost`


