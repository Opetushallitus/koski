import React from 'baret'
import Text from '../i18n/Text'
import '../style/main.less'

export default ({ memberName, onAcceptClick, logoutURL }) => (
  <div>
    <div className="acceptance-box">
      <div className="acceptance-title">
        <Text name="Antamalla suostumuksesi sallit, että Opetushallitus luovuttaa sinua koskevia henkilötietoja seuraavalle palveluntarjoajalle" />
      </div>
      <div className="acceptance-member-name">
        <Text name={memberName} />
      </div>
      <div className="acceptance-share-info">
        <Text name="Palveluntarjoajalle luovutetaan seuraavat henkilötiedot" />
        {':'}
        <ul>
          <li>
            <Text name="Oppijanumerosi" />
          </li>
          <li>
            <Text name="Seuraavat tiedot opiskeluoikeuksistasi" />
            {':'}
            <ul>
              <li>
                <Text name="Tiedot oppilaitoksesta, jossa opiskeluoikeutesi on" />
              </li>
              <li>
                <Text name="Opiskeluoikeuden alkamis- ja päättymispäivämäärät sekä läsnäolotiedot" />
              </li>
              <li>
                <Text name="Opiskeluoikeutesi koulutusaste sekä suorituksen tavoite (opiskeletko tutkintoa vai muuta tarkoitusta varten)" />
              </li>
              <li>
                <Text name="Ammatillisista opiskeluoikeuksista tiedot osa-aikaisuusjaksoista sekä oppisopimuksista ja koulutussopimuksista" />
              </li>
            </ul>
          </li>
          <li>
            <Text name="Suostumuksesi päättymisajankohta (suostumuksen voimassaoloaika on 12 kk ellet peru suostumustasi aiemmin)" />
          </li>
        </ul>
      </div>
      <div className="acceptance-paragraphs">
        <p>
          <Text name="Tarkemmat tiedot luovutettavista henkilötiedoista löydät KOSKI-palvelun Wiki -sivustolta" />
        </p>
        <p>
          <a href="https://wiki.eduuni.fi/display/OPHPALV/Oma+Data%3A+Opiskelijastatus+-+palautuvat+tiedot">
            <Text name="Tietoa KOSKI-palvelun pyytämistä henkilötiedoista" />
          </a>
        </p>
        <p>
          <Text name="Suostumuksesi päättymisajankohta (suostumuksen voimassaoloaika on 12 kk ellet peru suostumustasi aiemmin)" />
        </p>
        <p>
          <a href="https://wiki.eduuni.fi/display/OPHPALV/Oma+Data%3A+Opiskelijastatus+-+palautuvat+tiedot">
            <Text name="Tietoa KOSKI-palvelun pyytämistä henkilötiedoista" />
          </a>
        </p>
        <p>
          <Text name="Omat opiskeluoikeustietosi voit tarkistaa Oma Opintopolku-sivustolla" />
        </p>
        <p>
          <a href="https://opintopolku.fi/koski/omattiedot">
            <Text name="Oma Opintopolussa olevat omat opintosuorituksesi" />
          </a>
        </p>
        <p>
          <Text name="Palveluntarjoaja käyttää tietojasi opiskelija-asemasi todentamiseen opiskelijahintaisen matkalipun myöntämisen yhteydessä..." />
        </p>
        <p>
          <Text name="Palveluntarjoajan päätös alennuksen myöntämisestä tehdään automaattisesti opiskelija-asemaa todennettaessa" />
        </p>
        <p>
          <Text name="Palveluntarjoaja ei antamasi suostumuksen perusteella luovuta henkilötietojasi eteenpäin muille tahoille" />
        </p>
        <p>
          <Text name="Suostumuksesi on voimassa 12 kuukautta, jonka jälkeen voit uusia sen. Voit perua suostumuksesi milloin tahansa Oma Opintopolku -palvelussa tai palveluntarjoajan verkkopalvelun kautta" />
        </p>
        <p>
          <Text name="Kun suostumuksen voimassaolo on päättynyt tai peruutettu, Opetushallitus ei enää luovuta henkilötietojasi palveluntarjoajalle..." />
        </p>
        <p>
          <Text name="Lisätietoja palveluntarjoajan suorittamasta tietojen käsittelystä saat HSL:n verkkosivuilta" />
        </p>
        <p>
          <a href="https://www.hsl.fi/tietosuoja">
            <Text name="HSL:n verkkopalvelun tietosuojaseloste" />
          </a>
        </p>
        <p>
          <Text name="Lisätietoja Opetushallituksen suorittamasta tietojen käsittelystä saat Opintopolku-palvelusta" />
        </p>
        <p>
          <a href="https://opintopolku.fi/wp/tietosuojaseloste/koski-palvelun-tietosuojaseloste/">
            <Text name="KOSKI-palvelun tietosuojaseloste Opintopolku-sivustolla" />
          </a>
        </p>
      </div>
    </div>
    <div className="acceptance-button-container">
      <button
        className="acceptance-button koski-button"
        onClick={onAcceptClick}
      >
        <Text name="Hyväksy" />
      </button>
      <div className="decline-link">
        <a href={logoutURL}>
          <Text name="Peruuta ja palaa" />
        </a>
      </div>
    </div>
  </div>
)
