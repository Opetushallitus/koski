## Tietomalli

Käytettävällä JSON-formaatilla voidaan siirtää tietoja perusopetusta, lukiokoulutusta ja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä Koskeen ja eteenpäin tietoja tarvitsevien viranomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin.

#### JSON Schema

Käytettävä JSON-dataformaatti on kuvattu [JSON-schemalla](http://json-schema.org/), jota vasten siirretyt tiedot voidaan myös automaattisesti validoida.

<div class="preview-image-links">
  <a href="/koski/json-schema-viewer#koski-oppija-schema.json">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-preview.png">
    </div>
    <div class="caption">Visualisoitu JSON-schema</div>
    <p>Voi tarkastella schemaa visualisointityökalun avulla. Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten. Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.</p>
  </a>
  <a href="/koski/dokumentaatio/koski-oppija-schema.html">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-html-preview.png">
    </div>
    <div class="caption">Printattava dokumentti</div>
    <p>Printattava versio schemasta</p>
  </a>
  <a href="/koski/api/documentation/koski-oppija-schema.json">
    <div class="img-wrapper">
      <image src="/koski/images/koski-schema-json-preview.png">
    </div>
    <div class="caption">Lataa JSON-tiedostona</div>
    <p>Voit myös ladata scheman tiedostona</p>
  </a>
</div>

Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa [Koodistopalvelua](https://github.com/Opetushallitus/koodisto).
Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen [koulutuskoodi](/koski/dokumentaatio/koodisto/koulutus/latest).

Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä [scheman](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/schema/Oppija.scala),
että [esimerkkien](https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/documentation/Examples.scala) lähdekoodit.
