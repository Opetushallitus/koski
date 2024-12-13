import React from 'baret'
import {
  formatFinnishDate,
  ISO2FinnishDate,
  ISO2FinnishDateTime,
  parseISODate
} from '../../date/date'
import Text from '../../i18n/Text'
import { t } from '../../i18n/i18n'
import { useKoodisto } from '../../appstate/koodisto'

export const Kayttolupa = ({ kayttolupa, removeCallback }) => {
  const { asiakasName, asiakasId, expirationDate, timestamp, purpose } =
    kayttolupa
  const expDateInFinnish = formatFinnishDate(parseISODate(expirationDate))
  const timestampInFinnish = ISO2FinnishDate(timestamp)

  return (
    <li className="kayttolupa-container" tabIndex={0}>
      <h3 className="asiakas">{asiakasName}</h3>

      <div className="bottom-items">
        <div className="container">
          <div className="voimassaolo">
            <div className="teksti">
              <Text name="Lupa voimassa" />
            </div>
            <div className="aikaleima">
              <span className="mobile-whitespace">{': '}</span>
              <span> {`${timestampInFinnish} - ${expDateInFinnish}`}</span>
            </div>
          </div>

          <div className="oikeudet">
            <span className="list-label">
              <Text name="Palveluntarjoaja näkee seuraavat opintoihisi liittyvät tiedot" />
              {':'}
            </span>
            <ul>
              <li>
                <Text name="Tiedot opiskeluoikeuksistasi" />
              </li>
              <li>
                <Text name="Nimesi ja syntymäaikasi" />
              </li>
            </ul>
            {purpose && <Kayttotarkoitus purpose={purpose} />}
          </div>
        </div>

        <div className="peru-lupa">
          <button
            className="inline-link-button"
            onClick={() =>
              removeCallback({
                id: asiakasId,
                name: asiakasName,
                oauth2: false
              })
            }
          >
            <Text name="Peru lupa" />
          </button>
        </div>
      </div>
    </li>
  )
}

export const OAuth2Käyttölupa = ({ kayttolupa, removeCallback }) => {
  const { creationDate, expirationDate, scope } = kayttolupa
  const clientName = t(kayttolupa.clientName)
  const expDateInFinnish = ISO2FinnishDateTime(expirationDate)
  const timestampInFinnish = ISO2FinnishDateTime(creationDate)

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
    <li className="kayttolupa-container" tabIndex={0}>
      <h3 className="asiakas">{clientName}</h3>

      <div className="bottom-items">
        <div className="container">
          <div className="voimassaolo">
            <div className="teksti">
              <Text name="Lupa voimassa" />
            </div>
            <div className="aikaleima">
              <span className="mobile-whitespace">{': '}</span>
              <span> {`${timestampInFinnish} - ${expDateInFinnish}`}</span>
            </div>
          </div>

          <div className="oikeudet">
            <span className="list-label">
              <Text name="Palveluntarjoaja näkee seuraavat opintoihisi liittyvät tiedot" />
              {':'}
            </span>
            <ul>
              {scopes.map((s) => (
                <li key={s}>{localizedScope(s)}</li>
              ))}
            </ul>
          </div>
        </div>

        <div className="peru-lupa">
          <button
            className="inline-link-button"
            onClick={() => {
              removeCallback({
                id: kayttolupa.codeSHA256,
                name: clientName,
                oauth2: true
              })
            }}
          >
            <Text name="Peru lupa" />
          </button>
        </div>
      </div>
    </li>
  )
}

const Kayttotarkoitus = ({ purpose }) => (
  <div className="kayttotarkoitus">
    <div className="headline-container">
      <span className="headline">
        <Text name="Tietojen käyttötarkoitus" />
        {':'}
      </span>
    </div>
    <Text name={purpose} />
  </div>
)

export const NoMyDataPermissions = () => (
  <li className="no-permission">
    <Text
      name={
        'Et ole tällä hetkellä antanut millekään palveluntarjoajalle lupaa nähdä opintotietojasi Oma Opintopolusta. ' +
        'Luvan myöntäminen tapahtuu kyseisen palvelutarjoajan sivun kautta.'
      }
    />
  </li>
)
