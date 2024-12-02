import React from 'react'
import { t } from '../../i18n/i18n'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { IBPäätasonSuoritus } from '../../types/fi/oph/koski/schema/IBPaatasonSuoritus'
import { isMuidenLukioOpintojenPreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/MuidenLukioOpintojenPreIBSuoritus2019'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { isValinnanMahdollisuus } from '../../types/fi/oph/koski/schema/ValinnanMahdollisuus'
import { parasArviointi } from '../../util/arvioinnit'
import { sum } from '../../util/numbers'
import { KoulutusmoduuliOf, OsasuoritusOf } from '../../util/schema'
import { suoritusValmis } from '../../util/suoritus'
import { notUndefined } from '../../util/util'

// Vain OppiaineTablen tukemat päätason suoritukset (tätä komponenttia tullaan myöhemmin käyttämään ainakin lukion näkymille)
export type OppiainePäätasonSuoritus = IBPäätasonSuoritus

export type OppiaineOsasuoritus = OsasuoritusOf<OppiainePäätasonSuoritus>

export type OppiaineTableProps = {
  suoritus: OppiainePäätasonSuoritus
}

export const OppiaineTable: React.FC<OppiaineTableProps> = ({ suoritus }) => {
  const oppiaineet = suoritus.osasuoritukset || []

  return oppiaineet.length === 0 ? null : (
    <table className="OppiaineTable">
      <thead>
        <tr>
          <th></th>
          <th className="OppiaineTable__oppiaine">{t('Oppiaine')}</th>
          <th className="OppiaineTable__laajuus">{t('Laajuus (kurssia)')}</th>
          <th className="OppiaineTable__arvosana">{t('Arvosana')}</th>
        </tr>
      </thead>
      <tbody>
        {oppiaineet.map((oppiaine, i) => (
          <OppiaineRow key={i} oppiaine={oppiaine} />
        ))}
      </tbody>
    </table>
  )
}

type OppiaineRowProps = {
  oppiaine: OppiaineOsasuoritus
}

const OppiaineRow: React.FC<OppiaineRowProps> = ({ oppiaine }) => {
  const kurssit = oppiaine.osasuoritukset || []

  return (
    <tr>
      <td className="OppiaineRow__icon">
        <SuorituksenTilaIcon suoritus={oppiaine} />
      </td>
      <td className="OppiaineRow__oppiaine">
        <div className="OppiaineRow__nimi">
          {oppiaineenNimi(oppiaine.koulutusmoduuli)}
        </div>
        <div className="OppiaineRow__kurssit">
          {kurssit.map((kurssi, index) => (
            <Kurssi key={index} kurssi={kurssi} />
          ))}
        </div>
      </td>
      <td className="OppiaineRow__laajuus">{kurssit.length}</td>
      <td className="OppiaineRow__arvosana">{oppiaineenArvosana(oppiaine)}</td>
    </tr>
  )
}

const oppiaineenNimi = (
  koulutusmoduuli: KoulutusmoduuliOf<OppiaineOsasuoritus>
) =>
  [
    koulutusmoduuli.tunniste.nimi,
    (koulutusmoduuli as any)?.kieli?.nimi,
    (koulutusmoduuli as any)?.oppimäärä?.nimi
  ]
    .filter(notUndefined)
    .map((s) => t(s))
    .join(', ')

const oppiaineenArvosana = (oppiaine: OppiaineOsasuoritus) =>
  isMuidenLukioOpintojenPreIBSuoritus2019(oppiaine) || !oppiaine.arviointi
    ? null
    : parasArviointi(oppiaine.arviointi as Arviointi[])?.arvosana.koodiarvo

type KurssiProps = {
  kurssi: OsasuoritusOf<OppiaineOsasuoritus>
}

const Kurssi: React.FC<KurssiProps> = ({ kurssi }) => {
  return (
    <div className="Kurssi">
      <div className="Kurssi__tunniste">
        {kurssi.koulutusmoduuli.tunniste.koodiarvo}
      </div>
      <div className="Kurssi__arvosana">
        {kurssi.arviointi
          ? parasArviointi(kurssi.arviointi as Arviointi[])?.arvosana.koodiarvo
          : null}
      </div>
    </div>
  )
}

type SuorituksenTilaIconProps = {
  suoritus: Suoritus
}

const SuorituksenTilaIcon: React.FC<SuorituksenTilaIconProps> = ({
  suoritus
}) =>
  isValinnanMahdollisuus(suoritus) ? null : suoritusValmis(suoritus) ? (
    // eslint-disable-next-line react/jsx-no-literals
    <div title={t('Suoritus valmis')}>&#61452;</div>
  ) : (
    // eslint-disable-next-line react/jsx-no-literals
    <div title={t('Suoritus kesken')}>&#62034;</div>
  )
