# scala-schema (vendored)

Vendored copy of [Opetushallitus/scala-schema](https://github.com/Opetushallitus/scala-schema),
branch `scala-2.13`, commit `ebfb5f2744392a36b34cc1443e0c0ae060483e3b` ("Release 2.45.0_2.13").

The upstream repository is archived. Koski is the only consumer — edit the schema library
directly here; there is no upstream sync and no jitpack publishing. Package stays `fi.oph.scalaschema`.

These sources are compiled as part of koski via `build-helper-maven-plugin` (extra source roots
`scala-schema/src/main/scala` and `scala-schema/src/test/scala` + `src/test/java` in the root `pom.xml`)
— there is no separate Maven artifact.

Run its test suite: `make schematest`.
