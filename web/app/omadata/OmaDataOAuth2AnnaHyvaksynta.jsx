import React from 'baret'
import Text from '../i18n/Text'
import { useKoodisto } from '../appstate/koodisto'
import { t } from '../i18n/i18n'

import(/* webpackChunkName: "styles" */ '../style/main.less')

export default ({ clientName, onAcceptClick, onDeclineClick, scope }) => {
  const scopesKoodisto = useKoodisto('omadataoauth2scope')

  const localizedScope = (koodi) => {
    if (scopesKoodisto === null) {
      return koodi
    }
    const koodistoRecord = scopesKoodisto.find(
      (k) => k.koodiviite.koodiarvo === koodi.toLowerCase().replaceAll('_', '')
    )
    return koodistoRecord ? t(koodistoRecord.koodiviite.nimi) : koodi
  }

  const scopes = scope.split(' ')

  return (
    <div>
      <div className="acceptance-box">
        <div className="acceptance-title">
          <Text
            name={
              'TODO: TOR-2210 OmaDataOAuth2 -spesifit lakitekstit ja scope:n purku tälle sivulle'
            }
          />
          <Text name="Antamalla suostumuksesi sallit, että Opetushallitus luovuttaa sinua koskevia henkilötietoja seuraavalle palveluntarjoajalle" />
        </div>
        <div className="acceptance-member-name">
          <Text name={t(clientName)} />
        </div>
        <div className="acceptance-share-info">
          <Text name="Palveluntarjoajalle luovutetaan seuraavat henkilötiedot" />
          {':'}
          <ul>
            {scopes.map((s) => (
              <li>{localizedScope(s)}</li>
            ))}
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
            <a href="https://wiki.eduuni.fi/display/OPHPALV/TODO">
              <Text name="TODO: TOR-2210 Tietoa KOSKI-palvelun luovuttamista henkilötiedoista" />
            </a>
          </p>
          <p>
            <Text name="Suostumuksesi päättymisajankohta (suostumuksen voimassaoloaika on 12 kk ellet peru suostumustasi aiemmin)" />
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
            <Text name="TODO: TOR-2210: Palveluntarjoaja käyttää tietojasi ..." />
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
            <Text name="TODO: TOR-2210 Lisätietoja palveluntarjoajan suorittamasta tietojen käsittelystä saat verkkosivulta..." />
          </p>
          <p>
            <a href="https://todo">
              <Text name="TODO" />
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
        <button
          className="decline-button koski-button"
          onClick={onDeclineClick}
        >
          <Text name="Peruuta ja palaa" />
        </button>
      </div>
    </div>
  )
}
