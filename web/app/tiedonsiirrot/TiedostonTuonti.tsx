import React, { useRef, useState } from 'react'
import { ApiFailure, useApiMethod } from '../api-fetch'
import { TestIdRoot } from '../appstate/useTestId'
import { useVirkailijaUser } from '../appstate/user'
import Link from '../components/Link'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Spinner } from '../components-v2/texts/Spinner'
import { t, tExists } from '../i18n/i18n'
import { IbisParseError } from '../types/fi/oph/koski/ibis/IbisParseError'
import { IbisTuonninRivi } from '../types/fi/oph/koski/ibis/IbisTuonninRivi'
import { IbisTuonninTulos } from '../types/fi/oph/koski/ibis/IbisTuonninTulos'
import { esikatseleIbisTiedosto, tuoIbisTiedosto } from '../util/koskiApi'
import { TiedonsiirrotNavi, tiedostonTuontiPath } from './TiedonsiirrotNavi'

// Tilat ovat IbisTuonninRivinTila-olion arvot.
const tilanTeksti: Record<string, { esikatselu: string; tuonti: string }> = {
  uusi: { esikatselu: 'Luodaan', tuonti: 'Luotu' },
  päivitys: { esikatselu: 'Päivitetään', tuonti: 'Päivitetty' },
  hylätty: { esikatselu: 'Hylätään', tuonti: 'Hylätty' }
}

// Kokelaan tiedot (15 kenttää) ja niiden jälkeen kunkin oppiaineen 6 kenttää.
const esimerkkirivi = [
  ['2026', 'MAY', '000001', 'abc123', '01/01/2008', 'FEMALE', 'DIPLOMA'],
  ['Esimerkki', 'Erkki', '', '', '06/07/2026', '37', 'D', '+2'],
  ['2026', 'MAY', '112726', 'HL', '7', 'N'],
  ['2026', 'MAY', '100179', 'HL', '6', 'N'],
  ['2026', 'MAY', '100680', 'HL', '6', 'N'],
  ['2026', 'MAY', '100088', 'SL', '6', 'N'],
  ['2026', 'MAY', '166711', 'SL', '5', 'N'],
  ['2026', 'MAY', '100452', 'SL', '5', 'N'],
  ['2026', 'MAY', '100680', 'EE', 'B', 'N'],
  ['2026', 'MAY', '100577', 'TK', 'A', 'N']
]
  .flat()
  .map((kenttä) => `"${kenttä}"`)
  .join(',')

export const TiedostonTuonti: React.FC = () => {
  const user = useVirkailijaUser()
  return (
    <div className="content-area tiedonsiirrot">
      <TiedonsiirrotNavi location={tiedostonTuontiPath} />
      <div className="main-content tiedonsiirrot-content tiedoston-tuonti">
        <h2>{t('IBIS-tuonti')}</h2>
        {user?.hasPääkäyttäjäAccess ? (
          <TestIdRoot id="tiedoston-tuonti">
            <IbisTiedostonTuonti />
          </TestIdRoot>
        ) : (
          <p>{t('Sallittu vain OPH-pääkäyttäjälle')}</p>
        )}
      </div>
    </div>
  )
}

const IbisTiedostonTuonti: React.FC = () => {
  const tiedostokenttä = useRef<HTMLInputElement>(null)
  const [tiedosto, setTiedosto] = useState<File>()
  const esikatselu = useApiMethod(esikatseleIbisTiedosto)
  const tuonti = useApiMethod(tuoIbisTiedosto)
  const käsitellään =
    esikatselu.state === 'loading' || tuonti.state === 'loading'
  const tuotu = tuonti.state === 'success'
  const tuotaviaRivejä =
    esikatselu.state === 'success' &&
    esikatselu.data.rivit.some((rivi) => rivi.tila !== 'hylätty')

  const valitseTiedosto = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTiedosto(event.target.files?.[0])
    esikatselu.clear()
    tuonti.clear()
  }

  const tyhjennä = () => {
    if (tiedostokenttä.current) {
      tiedostokenttä.current.value = ''
    }
    setTiedosto(undefined)
    esikatselu.clear()
    tuonti.clear()
  }

  return (
    <>
      <Ohjeet teksti={t('ibis-tuonti-ohjeet')} />
      <h3>{t('Tiedoston muoto')}</h3>
      <Ohjeet teksti={t('ibis-tuonti-tiedoston-muoto')} />
      <details
        className="tiedoston-tuonti__esimerkki"
        data-testid="tiedoston-tuonti.esimerkki"
      >
        <summary>{t('Esimerkki')}</summary>
        <p>{t('ibis-tuonti-esimerkki-kuvaus')}</p>
        <pre>
          <code>{esimerkkirivi}</code>
        </pre>
      </details>
      <div className="tiedoston-tuonti__valinta">
        <label>
          {t('Valitse tiedosto')}
          <input
            ref={tiedostokenttä}
            type="file"
            accept=".csv,text/csv"
            onChange={valitseTiedosto}
            disabled={käsitellään}
            data-testid="tiedoston-tuonti.tiedosto"
          />
        </label>
      </div>
      {tiedosto && (
        <div className="tiedoston-tuonti__toiminnot">
          {esikatselu.state !== 'success' && (
            <RaisedButton
              onClick={() => esikatselu.call(tiedosto)}
              disabled={käsitellään}
              testId="esikatsele"
            >
              {t('Esikatsele')}
            </RaisedButton>
          )}
          {tuotaviaRivejä && (
            <RaisedButton
              onClick={() => tuonti.call(tiedosto)}
              disabled={tuotu || käsitellään}
              testId="tuo"
            >
              {t('Tuo')}
            </RaisedButton>
          )}
          <FlatButton
            onClick={tyhjennä}
            disabled={käsitellään}
            testId="tyhjennä"
          >
            {t('Tyhjennä')}
          </FlatButton>
        </div>
      )}
      {käsitellään && <Spinner />}
      {esikatselu.state === 'error' && <Virheilmoitus virhe={esikatselu} />}
      {tuonti.state === 'error' && <Virheilmoitus virhe={tuonti} />}
      {tuotu ? (
        <Tulos tulos={tuonti.data} />
      ) : (
        esikatselu.state === 'success' && <Tulos tulos={esikatselu.data} />
      )}
    </>
  )
}

// Lokalisoidun ohjetekstin jokainen rivi on oma kohtansa.
const Ohjeet: React.FC<{ teksti: string }> = ({ teksti }) => (
  <ul className="tiedoston-tuonti__ohjeet">
    {teksti.split('\n').map((kohta, index) => (
      <li key={index}>{kohta}</li>
    ))}
  </ul>
)

const Virheilmoitus: React.FC<{ virhe: ApiFailure }> = ({ virhe }) => (
  <div
    className="error tiedoston-tuonti__virheilmoitus"
    role="alert"
    data-testid="tiedoston-tuonti.virhe"
  >
    {t('Tiedoston käsittely epäonnistui')}
    <ul>
      {virhe.errors.map((error, index) => (
        <li key={index}>
          {tExists(error.messageKey) ? t(error.messageKey) : error.messageKey}
        </li>
      ))}
    </ul>
  </div>
)

const Tulos: React.FC<{ tulos: IbisTuonninTulos }> = ({ tulos }) => {
  const määrä = (tila: string) =>
    tulos.rivit.filter((rivi) => rivi.tila === tila).length
  return (
    <section
      className={`tiedoston-tuonti__tulos tiedoston-tuonti__tulos--${tulos.tallennettu ? 'tuonti' : 'esikatselu'}`}
      data-testid="tiedoston-tuonti.tulos"
    >
      <h3>{t(tulos.tallennettu ? 'Tuonnin tulos' : 'Esikatselun tulos')}</h3>
      <p
        className="tiedoston-tuonti__tulosilmoitus"
        data-testid="tiedoston-tuonti.tulosilmoitus"
      >
        {t(
          tulos.tallennettu
            ? 'Tuonti on tehty: hyväksytyt rivit on tallennettu Koskeen.'
            : 'Tämä on esikatselu: tietoja ei ole tallennettu Koskeen.'
        )}
      </p>
      <dl className="tiedoston-tuonti__yhteenveto">
        <dt>
          {t(tulos.tallennettu ? 'Luodut diplomat' : 'Luotavat diplomat')}
        </dt>
        <dd data-testid="tiedoston-tuonti.uudet">{määrä('uusi')}</dd>
        <dt>
          {t(
            tulos.tallennettu ? 'Päivitetyt diplomat' : 'Päivitettävät diplomat'
          )}
        </dt>
        <dd data-testid="tiedoston-tuonti.päivitykset">{määrä('päivitys')}</dd>
        <dt>{t(tulos.tallennettu ? 'Hylätyt rivit' : 'Hylättävät rivit')}</dt>
        <dd data-testid="tiedoston-tuonti.hylätyt">{määrä('hylätty')}</dd>
        <dt>{t('Jäsentämättömät rivit')}</dt>
        <dd data-testid="tiedoston-tuonti.jäsentämättömät">
          {tulos.jäsennysvirheet.length}
        </dd>
      </dl>
      {tulos.rivit.length === 0 && tulos.jäsennysvirheet.length === 0 && (
        <p>{t('Tiedostossa ei ole rivejä.')}</p>
      )}
      {tulos.rivit.length > 0 && (
        <Rivit rivit={tulos.rivit} tallennettu={tulos.tallennettu} />
      )}
      {tulos.jäsennysvirheet.length > 0 && (
        <Jäsennysvirheet virheet={tulos.jäsennysvirheet} />
      )}
    </section>
  )
}

type RivitProps = {
  rivit: IbisTuonninRivi[]
  tallennettu: boolean
}

const Rivit: React.FC<RivitProps> = ({ rivit, tallennettu }) => (
  <table data-testid="tiedoston-tuonti.rivit">
    <thead>
      <tr>
        <th>{t('Ibo tunniste')}</th>
        <th>{t('Tila')}</th>
        <th>{t('Oppija')}</th>
        <th>{t('Diploma')}</th>
        <th>{t('Virheet')}</th>
        <th>{t('Varoitukset')}</th>
      </tr>
    </thead>
    <tbody>
      {rivit.map((rivi, index) => (
        <tr key={index}>
          <td>{rivi.iboTunniste}</td>
          <td className={`tiedoston-tuonti__tila--${rivi.tila}`}>
            {t(
              tilanTeksti[rivi.tila]?.[tallennettu ? 'tuonti' : 'esikatselu'] ||
                rivi.tila
            )}
          </td>
          <td>
            {rivi.oppijaOid && (
              <Link href={`/koski/oppija/${rivi.oppijaOid}`}>
                {rivi.oppijaOid}
              </Link>
            )}
          </td>
          <td>{rivi.diplomanOid}</td>
          <td>
            <Lista tekstit={rivi.virheet} />
          </td>
          <td>
            <Lista tekstit={rivi.varoitukset} />
          </td>
        </tr>
      ))}
    </tbody>
  </table>
)

const Jäsennysvirheet: React.FC<{ virheet: IbisParseError[] }> = ({
  virheet
}) => (
  <>
    <h4>{t('Jäsentämättömät rivit')}</h4>
    <table data-testid="tiedoston-tuonti.jäsennysvirheet">
      <thead>
        <tr>
          <th>{t('Rivi')}</th>
          <th>{t('Virhe')}</th>
        </tr>
      </thead>
      <tbody>
        {virheet.map((virhe) => (
          <tr key={virhe.line}>
            <td>{virhe.line}</td>
            <td>{virhe.message}</td>
          </tr>
        ))}
      </tbody>
    </table>
  </>
)

const Lista: React.FC<{ tekstit: string[] }> = ({ tekstit }) =>
  tekstit.length > 0 ? (
    <ul className="cell-listing">
      {tekstit.map((teksti, index) => (
        <li key={index}>{teksti}</li>
      ))}
    </ul>
  ) : null
