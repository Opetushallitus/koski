## Valpas

Valpas on palvelu oppivelvollisuuden seurantaan ja valvontaan.

Lisätietoja Valpas-järjestelmästä löydät [Opetushallituksen wiki-sivustolta](https://wiki.eduuni.fi/spaces/ophPPK/pages/197666912/Valpas-palvelu).

Teknisesti Valpas on toteutettu osana Koski-järjestelmää. Palvelun lähdekoodit löytyvät [GitHubista](https://github.com/Opetushallitus/koski/tree/master/src/main/scala/fi/oph/koski/valpas).

### Sisäiset tietomallit

Valpas-palvelu käyttää sisäisiä tietomalleja oppijoiden tietojen esittämiseen. Valpas-palvelu koostaa tietoa Koski-tietovarannosta ja muista Opintopolun palveluista näyttämistä varten.

* [Yksittäisen oppijan näkymän tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-laaja-schema.json)
* [Hakeutumisvelvollisten, oppivelvollisuuden suorittamisen ja kunnalle tehtyjen ilmoitusten listanäkymien tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-suppea-schema.json)
* [Asuinkunnan valvojan ilmoitettujen oppivelvollisten listanäkymän tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-kunta-suppea-schema.json)
* [Asuinkunnan valvojan automaattisen tarkistusnäkymän tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-kuntarouhinta-schema.json)
* [Asuinkunnan valvojan henkilötunnusten perusteella tehtävän haun näkymän tietomalli.](/koski/json-schema-viewer/?schema=valpas-internal-heturouhinta-schema.json)
