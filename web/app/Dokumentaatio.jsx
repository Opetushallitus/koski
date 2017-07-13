import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import { contentWithLoadingIndicator } from './AjaxLoadingIndicator.jsx'

const addScript = url => {
  if (!document.querySelector('script[src="'+url+'"]')) {
    var script = document.createElement('script')
    script.src = url
    script.async = true
    document.body.appendChild(script)
  }
}

export const dokumentaatioContentP = () => contentWithLoadingIndicator(sectionsP()).map(s => {
  return ({
    content: (
      <div className='content-area'>

        <h1>{'Koski-tiedonsiirtoprotokolla'}</h1>
        <p>{'Tässä dokumentissa kuvataan Koski-järjestelmän tiedonsiirrossa käytettävä protokolla. Lisätietoja Koski-järjestelmästä löydät '}<a href="https://confluence.csc.fi/display/OPHPALV/Koski">{'Opetushallituksen wiki-sivustolta'}</a>{'. Järjestelmän lähdekoodit ja kehitysdokumentaatio '}<a href="https://github.com/Opetushallitus/koski">{'Githubissa'}</a>{'.'}</p>
        <p>{'Protokolla, kuten Koski-järjestelmäkin, on työn alla, joten kaikki voi vielä muuttua.'}</p>
        <p>{'Muutama perusasia tullee kuitenkin säilymään:'}</p>

        <ul>
          <li>{'Rajapinnan avulla järjestelmään voi tallentaa tietoja oppijoiden opinto-oikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa'}</li>
          <li>{'Rajapinnan avulla tietoja voi myös hakea ja muokata'}</li>
          <li>{'Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä. Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa kyseisen oppilaitoksen henkilöstölle'}</li>
          <li>{'Rajapinta mahdollistaa myös automaattiset tiedonsiirrot tietojärjstelmien välillä. Näin esimerkiksi tietyt viranomaiset voivat saada tietoja Koskesta. Samoin oppilaitoksen tietojärjestelmät voivat päivittää tietoja Koskeen.'}</li>
          <li>{'Järjestelmä tarjoaa REST-tyyppisen tiedonsiirtorajapinnan, jossa dataformaattina on JSON'}</li>
          <li>{'Samaa tiedonsiirtoprotokollaa ja dataformaattia pyritään soveltuvilta osin käyttämään sekä käyttöliittymille, jotka näyttävät tietoa loppukäyttäjille, että järjestelmien väliseen kommunikaatioon'}</li>
        </ul>

        <h2>{'JSON-dataformaatti'}</h2>

        <p>{'Käytettävästä JSON-formaatista on laadittu työversio, jonka toivotaan vastaavan ammatillisen koulutuksen tarpeisiin. Tällä formaatilla siis tulisi voida siirtää tietoja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä Koskeen ja eteenpäin tietoja tarvitsevien viramomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin. Formaattia on tarkoitus laajentaa soveltumaan myös muiden koulutustyyppien tarpeisiin, mutta näitä tarpeita ei ole vielä riittävällä tasolla kartoitettu, jotta konkreettista dataformaattia voitaisiin suunnitella. Yksi formaatin suunnittelukriteereistä on toki ollut sovellettavuus muihinkin koulutustyyppeihin.'}</p>

        <h3>{'JSON Schema'}</h3>

        <p>{'Käytettävä JSON-dataformaatti on kuvattu '}<a href="http://json-schema.org/">{'JSON-schemalla'}</a>{', jota vasten siirretyt tiedot voidaan myös automaattisesti validoida.'}</p>

        <div className="preview-image-links">
          <a href="${schemaViewerUrl}">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-preview.png"/>
            </div>
            <div className="caption">{'Visualisoitu JSON-schema'}</div>
            <p>{'Voi tarkastella schemaa visualisointityökalun avulla. Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten. Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.'}</p>
          </a>
          <a href="${schemaDocumentUrl} FIXME">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-html-preview.png"/>
            </div>
            <div className="caption">{'Printattava dokumentti'}</div>
            <p>{'Printattava versio schemasta'}</p>
          </a>
          <a href="${schemaFileUrl} FIXME">
            <div className="img-wrapper">
              <img src="/koski/images/koski-schema-json-preview.png"/>
            </div>
            <div className="caption">{'Lataa JSON-tiedostona'}</div>
            <p>{'Voit myös ladata scheman tiedostona'}</p>
          </a>
        </div>

        <p>{'Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa '}<a href="https://github.com/Opetushallitus/koodisto">{'Koodistopalvelua'}</a>{'. Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen '}<a href="/koski/documentation/koodisto/koulutus/latest">{'koulutuskoodi'}</a>{'.'}</p>
        <p>{'Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä '}<a href="https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/schema/Oppija.scala">{'scheman'}</a>{', että '}<a href="https://github.com/Opetushallitus/koski/blob/master/src/main/scala/fi/oph/koski/documentation/Examples.scala">{'esimerkkien'}</a>{' lähdekoodit.'}</p>


        <h2>{'REST-rajapinnat'}</h2>

        <p>{'Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.'}</p>
        <p>{'Rajapinnat on lueteltu ja kuvattu alla. Voit myös testata rajapintojen toimintaa tällä sivulla, kunhan käyt ensin '}<a href="/koski">{'kirjautumassa sisään'}</a>{' järjestelmään. Saat tarvittavat tunnukset Koski-kehitystiimiltä pyydettäessä.'}</p>
        <p>{'Rajapintojen käyttämät virhekoodit on myös kuvattu alla. Virhetapauksissa rajapinnat käyttävät alla kuvattuja HTTP-statuskoodeja ja sisällyttävät tarkemmat virhekoodit ja selitteineen JSON-tyyppiseen paluuviestiin. Samaan virhevastaukseen voi liittyä useampi virhekoodi/selite.'}</p>

        <h2>{'Esimerkkidata annotoituna'}</h2>
        <p>{"Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä. Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon."}</p>


        <div dangerouslySetInnerHTML={{__html: s}}></div>
      </div>
    ),
    title: 'Tiedonsiirrot'
  })
})


const sectionsP = () => Http.cachedGet('/koski/api/documentation/examples', { errorMapper: (e) => e.httpStatus === 404 ? null : new Bacon.Error}).toProperty()

// <script src='//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.1.0/highlight.min.js'></script>
//<script src='/koski/js/codemirror/codemirror.js'></script>
//<script src='/koski/js/codemirror/javascript.js'></script>
