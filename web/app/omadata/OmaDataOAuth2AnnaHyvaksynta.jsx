import React from 'baret'
import Text from '../i18n/Text'
import { useKoodisto } from '../appstate/koodisto'
import { t, tExists } from '../i18n/i18n'
import TextTemplate from '../i18n/TextTemplate'

import(/* webpackChunkName: "styles" */ '../style/main.less')

export default ({
  clientId,
  clientName,
  onAcceptClick,
  onDeclineClick,
  scope,
  durationInMin
}) => {
  return (
    <div>
      <div className="acceptance-box">
        <AcceptanceTitle clientName={clientName} />

        <ScopeList scope={scope} />

        <AcceptanceParagraphs
          durationInMin={durationInMin}
          clientId={clientId}
        />
      </div>

      <AcceptanceButtons
        onAcceptClick={onAcceptClick}
        onDeclineClick={onDeclineClick}
      />
    </div>
  )
}

const AcceptanceTitle = ({ clientName }) => {
  return (
    <>
      <div className="acceptance-title">
        <Text name="Antamalla suostumuksesi sallit, että Opetushallitus luovuttaa sinua koskevia henkilötietoja seuraavalle palveluntarjoajalle" />
      </div>
      <div className="acceptance-member-name">
        <span aria-label={t(clientName)}>{t(clientName)}</span>
      </div>
    </>
  )
}

const ScopeList = ({ scope }) => {
  const scopesKoodisto = useKoodisto('omadataoauth2scope')

  const localizedScope = (koodi) => {
    if (scopesKoodisto === null) {
      return koodi
    }
    const koodistoRecord = scopesKoodisto.find(
      (k) => k.koodiviite.koodiarvo === koodi.toLowerCase()
    )
    return koodistoRecord ? t(koodistoRecord.koodiviite.nimi) : koodi
  }

  const scopes = scope.split(' ')

  return (
    <div className="acceptance-share-info">
      <Text name="Palveluntarjoajalle luovutetaan seuraavat henkilötiedot" />
      {':'}
      <ul>
        {scopes.map((s) => (
          <li key={s}>{localizedScope(s)}</li>
        ))}
        <li>{t('omadataoauth2_suostumuksesi_paattymisajankohta')}</li>
      </ul>
    </div>
  )
}

const AcceptanceParagraphs = ({ durationInMin, clientId }) => {
  return (
    <div className="acceptance-paragraphs">
      <Paattymisajankohta durationInMin={durationInMin} />
      <p>
        <Text name="Omat opiskeluoikeustietosi voit tarkistaa Oma Opintopolku-sivustolla" />
      </p>
      <p>
        <a href="https://opintopolku.fi/koski/omattiedot" target="_blank">
          <Text name="Oma Opintopolussa olevat omat opintosuorituksesi" />
        </a>
      </p>

      <PalveluntarjoajakohtainenTeksti clientId={clientId} />
      <LinkkiPalveluntarjoajaan clientId={clientId} />

      <p>
        <Text name="Lisätietoja Opetushallituksen suorittamasta tietojen käsittelystä saat Opintopolku-palvelusta" />
      </p>
      <p>
        <a href={t('tietosuojaseloste-link')} target="_blank">
          <Text name="KOSKI-palvelun tietosuojaseloste Opintopolku-sivustolla" />
        </a>
      </p>
    </div>
  )
}

const Paattymisajankohta = ({ durationInMin }) => {
  return (
    <p>
      <TextTemplate
        templateName="omadataoauth2_suostumuksesi_paattymisajankohta_min"
        duration_in_minutes={durationInMin}
      />
    </p>
  )
}

const PalveluntarjoajakohtainenTeksti = ({ clientId }) => {
  return Array.from({ length: 9 }, (x, i) => {
    const paragraphId = `omadataoauth2_tekstikappale_${clientId}_${i + 1}`

    if (tExists(paragraphId)) {
      return (
        <p key={paragraphId}>
          <Text name={paragraphId} />
        </p>
      )
    } else {
      return null
    }
  })
}

const LinkkiPalveluntarjoajaan = ({ clientId }) => {
  const linkkiId = `omadataoauth2_linkki_${clientId}`
  const linkkitekstiId = `omadataoauth2_linkkiteksti_${clientId}`

  return tExists(linkkiId) && tExists(linkkitekstiId) ? (
    <>
      <p>
        <Text name="omadataoauth2_lisatietoja_palveluntarjoajalta_saat" />
      </p>
      <p>
        <a href={t(linkkiId)} target="_blank">
          <Text name={linkkitekstiId} />
        </a>
      </p>
    </>
  ) : null
}

const AcceptanceButtons = ({ onAcceptClick, onDeclineClick }) => {
  return (
    <div className="acceptance-button-container">
      <button
        className="acceptance-button koski-button"
        onClick={onAcceptClick}
      >
        <Text name="Hyväksy" />
      </button>
      <button className="decline-button koski-button" onClick={onDeclineClick}>
        <Text name="Peruuta ja palaa" />
      </button>
    </div>
  )
}
