import React from 'react'
import { useKoodisto } from '../appstate/koodisto'
import { Trans } from '../components-v2/texts/Trans'
import { t, tExists } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { buildLocalizedPaattymisajankohtaText } from './expirationTime'

export type OmaDataOAuth2AnnaHyvaksyntaProps = {
  clientId: string
  clientName: LocalizedString
  scope: string
  durationInMin: number
  onAcceptClick: () => void
  onDeclineClick: () => void
}

const OmaDataOAuth2AnnaHyvaksynta = ({
  clientId,
  clientName,
  onAcceptClick,
  onDeclineClick,
  scope,
  durationInMin
}: OmaDataOAuth2AnnaHyvaksyntaProps) => {
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

export default OmaDataOAuth2AnnaHyvaksynta

const AcceptanceTitle = ({ clientName }: { clientName: LocalizedString }) => {
  return (
    <>
      <div className="acceptance-title">
        <Trans>
          {
            'Antamalla suostumuksesi sallit, että Opetushallitus luovuttaa sinua koskevia henkilötietoja seuraavalle palveluntarjoajalle'
          }
        </Trans>
      </div>
      <div className="acceptance-member-name">
        <span aria-label={t(clientName)}>{t(clientName)}</span>
      </div>
    </>
  )
}

const ScopeList = ({ scope }: { scope: string }) => {
  const scopesKoodisto = useKoodisto('omadataoauth2scope')

  const localizedScope = (koodi: string) => {
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
      <Trans>{'Palveluntarjoajalle luovutetaan seuraavat henkilötiedot'}</Trans>
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

const AcceptanceParagraphs = ({
  durationInMin,
  clientId
}: {
  durationInMin: number
  clientId: string
}) => {
  return (
    <div className="acceptance-paragraphs">
      <PalveluntarjoajakohtainenKayttotarkoitusTeksti clientId={clientId} />

      <Paattymisajankohta durationInMin={durationInMin} />
      <p>
        <Trans>{'omadataoauth2_oma_opintopolku_linkin_esittely'}</Trans>{' '}
        <a
          href={t('omadataoauth2_oma_opintopolku_linkki')}
          target="_blank"
          rel="noopener noreferrer"
        >
          <Trans>{'omadataoauth2_oma_opintopolku_linkin_teksti'}</Trans>
        </a>
      </p>

      <PalveluntarjoajakohtainenTeksti clientId={clientId} />
      <LinkkiPalveluntarjoajaan clientId={clientId} />

      {
        // tämän ei haluta näkyvän kuin tietyille tahoille
        tExists(`omadataoauth2_oph_lisatietoja_${clientId}`) && (
          <>
            <p>
              <Trans>
                {
                  'Lisätietoja Opetushallituksen suorittamasta tietojen käsittelystä saat Opintopolku-palvelusta'
                }
              </Trans>
            </p>
            <p>
              <a
                href={t('tietosuojaseloste-link')}
                target="_blank"
                rel="noopener noreferrer"
              >
                <Trans>
                  {'KOSKI-palvelun tietosuojaseloste Opintopolku-sivustolla'}
                </Trans>
              </a>
            </p>
          </>
        )
      }
    </div>
  )
}

const Paattymisajankohta = ({ durationInMin }: { durationInMin: number }) => {
  const text = buildLocalizedPaattymisajankohtaText(durationInMin, t)

  return (
    <p>
      <span className="localized" aria-label={text}>
        {text}
      </span>
    </p>
  )
}

// Palveluntarjoajakohtaiset kappaleet ovat numeroituja lokalisointiavaimia, joista näytetään ne, jotka on määritelty
const numeroidutKappaleet = (avainPrefix: string) =>
  Array.from({ length: 9 }, (_, i) => `${avainPrefix}_${i + 1}`)
    .filter(tExists)
    .map((paragraphId) => (
      <p key={paragraphId}>
        <Trans>{paragraphId}</Trans>
      </p>
    ))

const PalveluntarjoajakohtainenKayttotarkoitusTeksti = ({
  clientId
}: {
  clientId: string
}) => (
  <>
    {numeroidutKappaleet(
      `omadataoauth2_tekstikappale_kayttotarkoitus_${clientId}`
    )}
  </>
)

const PalveluntarjoajakohtainenTeksti = ({
  clientId
}: {
  clientId: string
}) => <>{numeroidutKappaleet(`omadataoauth2_tekstikappale_${clientId}`)}</>

const LinkkiPalveluntarjoajaan = ({ clientId }: { clientId: string }) => {
  const linkkiId = `omadataoauth2_linkki_${clientId}`
  const linkkitekstiId = `omadataoauth2_linkkiteksti_${clientId}`

  return tExists(linkkiId) && tExists(linkkitekstiId) ? (
    <>
      <p>
        <Trans>{'omadataoauth2_lisatietoja_palveluntarjoajalta_saat'}</Trans>
      </p>
      <p>
        <a href={t(linkkiId)} target="_blank" rel="noopener noreferrer">
          <Trans>{linkkitekstiId}</Trans>
        </a>
      </p>
    </>
  ) : null
}

const AcceptanceButtons = ({
  onAcceptClick,
  onDeclineClick
}: {
  onAcceptClick: () => void
  onDeclineClick: () => void
}) => {
  return (
    <div className="acceptance-button-container">
      <button
        className="acceptance-button koski-button"
        onClick={onAcceptClick}
      >
        <Trans>{'Hyväksy'}</Trans>
      </button>
      <button className="decline-button koski-button" onClick={onDeclineClick}>
        <Trans>{'Peruuta ja palaa'}</Trans>
      </button>
    </div>
  )
}
