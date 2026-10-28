import React from 'react'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { LukionOpiskeluoikeus } from '../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { t } from '../i18n/i18n'
import { FootnoteDescriptions } from '../components/footnote'
import { formatNumber, removeFloatingPointDrift, sum } from '../util/numbers'
import { isLukionOppimääränSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppimaaranSuoritus2019'
import { isLukionOppiaineidenOppimäärienSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppiaineidenOppimaarienSuoritus2019'
import {
  isLukionOppiaineenSuoritus2019,
  LukionOppiaineenSuoritus2019
} from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019'
import { isLukionPaikallisenOpintojaksonSuoritus2019 } from '../types/fi/oph/koski/schema/LukionPaikallisenOpintojaksonSuoritus2019'
import { isPaikallinenLukionOppiaine2019 } from '../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2019'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { parasArviointi } from '../util/arvioinnit'
import { suoritusValmis } from '../util/suoritus'
import {
  CHARCODE_KESKEN,
  CHARCODE_VALMIS,
  Icon
} from '../components-v2/texts/Icon'

type LukionOppiaineetProps = {
  form: FormModel<LukionOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<LukionOpiskeluoikeus>
}

type ArviointiLike = {
  arvosana: { koodiarvo: string }
  hyväksytty?: boolean
}

const isHyväksytty = (arviointi?: ArviointiLike): boolean => {
  if (!arviointi) return false
  if (arviointi.hyväksytty !== undefined) return arviointi.hyväksytty
  return (
    arviointi.arvosana.koodiarvo !== '4' && arviointi.arvosana.koodiarvo !== 'H'
  )
}

const kurssiLaajuus = (
  kurssi: LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019
): LaajuusOpintopisteissä => kurssi.koulutusmoduuli.laajuus

const kurssinArviointi = (
  kurssi: LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019
) => parasArviointi(kurssi.arviointi as Arviointi[] | undefined)

const oppiaineenArvioituLaajuus = (
  suoritus: LukionOppiaineenSuoritus2019
): { arvioitu: number; hyväksytysti: number } => {
  const kurssit = suoritus.osasuoritukset || []
  const arvioidut = kurssit.filter((k) => kurssinArviointi(k) !== undefined)
  const hyväksytyt = arvioidut.filter((k) =>
    isHyväksytty(kurssinArviointi(k) as ArviointiLike | undefined)
  )
  return {
    arvioitu: sum(arvioidut.map((k) => kurssiLaajuus(k).arvo)),
    hyväksytysti: sum(hyväksytyt.map((k) => kurssiLaajuus(k).arvo))
  }
}

// Vaihe 1: vain katselu. Tuetaan "Lukion oppimäärä" ja "Lukion aineopinnot"
// -suoritusten oppiaineita (LukionOppiaineenSuoritus2019). Muut lukion
// osasuoritukset (lukiodiplomit, temaattiset opinnot ym.
// MuidenLukioOpintojenSuoritus2019) jätetään vaiheen 1 ulkopuolelle - niitä ei
// ollut mukana tämän vaiheen kahdessa kohdeoppijassa.
//
// Rakennetaan oikealla <table>-elementillä OsasuoritusTablen (CSS Grid) sijaan.
// OsasuoritusTable mitoittaa jokaisen rivin ruudukon erikseen oman sisältönsä
// mukaan, joten lyhyen sisällön (esim. yksinumeroinen arvosana) sarakkeet eivät
// osu kohdalleen otsikon tai muiden rivien kanssa - <table> taas mitoittaa
// sarakkeet koko taulukon yli yhtenäisesti, kuten vanhassa käyttöliittymässä.
// Sama ratkaisu kuin components-v2/opiskeluoikeus/OppiaineTable.tsx:ssä (IB).
export const LukionOppiaineet: React.FC<LukionOppiaineetProps> = ({
  päätasonSuoritus
}) => {
  const suoritus = päätasonSuoritus.suoritus

  if (
    !isLukionOppimääränSuoritus2019(suoritus) &&
    !isLukionOppiaineidenOppimäärienSuoritus2019(suoritus)
  ) {
    return null
  }

  const osasuoritukset = (suoritus.osasuoritukset || []).filter(
    isLukionOppiaineenSuoritus2019
  )

  return (
    <div className="LukionOppiaineet">
      <table className="LukionOppiaineet__taulukko">
        <thead>
          <tr>
            <th className="LukionOppiaineet__tila" />
            <th className="LukionOppiaineet__oppiaine">{t('Oppiaine')}</th>
            <th className="LukionOppiaineet__laajuus">
              {t('Arvioitu (opintopistettä)')}
            </th>
            <th className="LukionOppiaineet__laajuus">
              {t('Hyväksytysti arvioitu (opintopistettä)')}
            </th>
            <th className="LukionOppiaineet__arvosana">{t('Arvosana')}</th>
          </tr>
        </thead>
        <TestIdLayer id="osasuoritukset">
          <tbody>
            {osasuoritukset.map((oppiaine, i) => (
              <TestIdLayer id={i} key={i}>
                <OppiaineRivi suoritus={oppiaine} />
              </TestIdLayer>
            ))}
          </tbody>
        </TestIdLayer>
      </table>
      {onPaikallisia(osasuoritukset) && (
        <FootnoteDescriptions
          data={[
            {
              title: 'Paikallinen opintojakso tai oppiaine',
              hint: PAIKALLINEN_HINT
            }
          ]}
        />
      )}
      {osasuoritukset.some((s) => s.suoritettuErityisenäTutkintona) && (
        <FootnoteDescriptions
          data={[
            {
              title: 'Erityisenä tutkintona suoritettu oppiaine',
              hint: ERITYINEN_TUTKINTO_HINT
            }
          ]}
        />
      )}
      <KurssienLaajuusYhteensä osasuoritukset={osasuoritukset} />
    </div>
  )
}

const OppiaineRivi: React.FC<{ suoritus: LukionOppiaineenSuoritus2019 }> = ({
  suoritus
}) => {
  const kurssit = suoritus.osasuoritukset || []
  const { arvioitu, hyväksytysti } = oppiaineenArvioituLaajuus(suoritus)
  const paras = parasArviointi(suoritus.arviointi as Arviointi[] | undefined)
  const valmis = suoritusValmis(suoritus)

  return (
    <tr>
      <td className="LukionOppiaineet__tila">
        {valmis ? (
          <span title={t('Suoritus valmis')}>
            <Icon charCode={CHARCODE_VALMIS} />
          </span>
        ) : (
          <span title={t('Suoritus kesken')}>
            <Icon charCode={CHARCODE_KESKEN} />
          </span>
        )}
      </td>
      <td className="LukionOppiaineet__oppiaine">
        <TestIdText id="nimi">{oppiaineNimi(suoritus)}</TestIdText>
        <OppiaineFootnoteHints suoritus={suoritus} />
        {kurssit.length > 0 && <KurssiLista kurssit={kurssit} />}
      </td>
      <td className="LukionOppiaineet__laajuus">
        <TestIdText id="laajuus.arvioitu">{formatNumber(arvioitu)}</TestIdText>
      </td>
      <td className="LukionOppiaineet__laajuus">
        <TestIdText id="laajuus.hyvaksytystiArvioitu">
          {formatNumber(hyväksytysti)}
        </TestIdText>
      </td>
      <td className="LukionOppiaineet__arvosana">
        <TestIdText id="arvosana.value">
          {paras ? paras.arvosana.koodiarvo : ''}
        </TestIdText>
      </td>
    </tr>
  )
}

const PAIKALLINEN_HINT = '*'
const ERITYINEN_TUTKINTO_HINT = '**'

const OppiaineFootnoteHints: React.FC<{
  suoritus: LukionOppiaineenSuoritus2019
}> = ({ suoritus }) => {
  const hints = [
    isPaikallinenLukionOppiaine2019(suoritus.koulutusmoduuli) &&
      PAIKALLINEN_HINT,
    suoritus.suoritettuErityisenäTutkintona && ERITYINEN_TUTKINTO_HINT
  ].filter(Boolean) as string[]

  return hints.length > 0 ? (
    <sup className="footnote-hint">{` ${hints.join(' ')}`}</sup>
  ) : null
}

// Kurssit/opintojaksot näytetään aina näkyvissä oppiaineen nimen alla samoin
// kuin vanhassa käyttöliittymässä (kurssi/KurssiEditor.jsx). Samat CSS-luokat
// (.kurssit/.kurssi/.tunniste/.arvosana) tulevat globaalista
// style/kurssi.less:stä, joten ulkoasu vastaa vanhaa käyttöliittymää ilman
// uutta tyylittelyä.
const KurssiLista: React.FC<{
  kurssit: LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019[]
}> = ({ kurssit }) => (
  <span className="kurssit">
    <ul>
      {kurssit.map((kurssi, i) => {
        const arviointi = kurssinArviointi(kurssi)
        const paikallinen = isLukionPaikallisenOpintojaksonSuoritus2019(kurssi)
        return (
          <li className="kurssi" key={i}>
            <span
              className="tunniste"
              title={t(kurssi.koulutusmoduuli.tunniste.nimi)}
            >
              {kurssi.koulutusmoduuli.tunniste.koodiarvo}
            </span>
            {paikallinen && (
              <sup className="footnote-hint">{PAIKALLINEN_HINT}</sup>
            )}
            <div className="arvosana">
              {arviointi ? arviointi.arvosana.koodiarvo : ''}
            </div>
          </li>
        )
      })}
    </ul>
  </span>
)

const onPaikallisia = (osasuoritukset: LukionOppiaineenSuoritus2019[]) =>
  osasuoritukset.some(
    (s) =>
      isPaikallinenLukionOppiaine2019(s.koulutusmoduuli) ||
      (s.osasuoritukset || []).some(isLukionPaikallisenOpintojaksonSuoritus2019)
  )

const KurssienLaajuusYhteensä: React.FC<{
  osasuoritukset: LukionOppiaineenSuoritus2019[]
}> = ({ osasuoritukset }) => {
  const kaikkiKurssit = osasuoritukset.flatMap((o) => o.osasuoritukset || [])
  const arvioidut = kaikkiKurssit.filter(
    (k) => kurssinArviointi(k) !== undefined
  )
  const hyväksytyt = arvioidut.filter((k) =>
    isHyväksytty(kurssinArviointi(k) as ArviointiLike | undefined)
  )
  if (arvioidut.length === 0) return null

  const arvioituYhteensä = removeFloatingPointDrift(
    sum(arvioidut.map((k) => kurssiLaajuus(k).arvo))
  )
  const hyväksytystiYhteensä = removeFloatingPointDrift(
    sum(hyväksytyt.map((k) => kurssiLaajuus(k).arvo))
  )

  return (
    <p className="kurssit-yhteensä">
      <TestIdText id="laajuus.arvioitu.yhteensa">
        {t('Arvioitujen osasuoritusten laajuus yhteensä')}
        {': '}
        {formatNumber(arvioituYhteensä)}
      </TestIdText>
      <TestIdText id="laajuus.hyvaksytystiArvioitu.yhteensa">
        {t('Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä')}
        {': '}
        {formatNumber(hyväksytystiYhteensä)}
      </TestIdText>
    </p>
  )
}

const oppiaineNimi = (suoritus: LukionOppiaineenSuoritus2019): string => {
  const nimi = t(suoritus.koulutusmoduuli.tunniste.nimi)
  const kieli =
    'kieli' in suoritus.koulutusmoduuli
      ? suoritus.koulutusmoduuli.kieli
      : undefined
  return kieli ? `${nimi}, ${t(kieli.nimi).toLowerCase()}` : nimi
}
