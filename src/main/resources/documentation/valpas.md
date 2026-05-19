## Valpas

Valpas on palvelu oppivelvollisuuden seurantaan ja valvontaan.

Lisätietoja Valpas-järjestelmästä löydät [Opetushallituksen wiki-sivustolta](https://wiki.eduuni.fi/spaces/ophPPK/pages/197666912/Valpas-palvelu).

Teknisesti Valpas on osa Koski-järjestelmää. Palvelun lähdekoodit löytyvät [GitHubista](https://github.com/Opetushallitus/koski/tree/master/src/main/scala/fi/oph/koski/valpas).

### Sisäiset tietomallit

Valpas-palvelu käyttää kolmea sisäistä tietomallia oppijoiden tietojen esittämiseen.

**[valpas-internal-laaja-schema.json](/koski/api/documentation/valpas-internal-laaja-schema.json)**

Laaja tietomalli sisältää oppijan tiedot laajassa muodossa ja sitä käytetään Valppaan yksittäisen oppijan näkymässä.

**[valpas-internal-suppea-schema.json](/koski/api/documentation/valpas-internal-suppea-schema.json)**

Suppea tietomalli sisältää oppijoiden tiedot rajatummassa muodossa. Sitä käytetään Valppaan hakeutumisvelvollisten, oppivelvollisuuden suorittamisen ja kunnalle tehtyjen ilmoitusten listanäkymissä.

**[valpas-internal-kunta-suppea-schema.json](/koski/api/documentation/valpas-internal-kunta-suppea-schema.json)**

Kuntailmoitukset sisältävää suppeaa tietomallia käytetään Valppaan asuinkunnan valvojan ilmoitettujen oppivelvollisten listanäkymässä.
