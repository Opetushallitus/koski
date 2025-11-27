import { TelmaJaValmaArviointi } from './TelmaJaValmaArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { ValmaKoulutuksenOsa } from './ValmaKoulutuksenOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'
import { Näyttö } from './Naytto'

/**
 * Suoritettavan VALMA-koulutuksen osan / osien tiedot
 *
 * @see `fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus`
 */
export type ValmaKoulutuksenOsanSuoritus = {
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus'
  arviointi?: Array<TelmaJaValmaArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: ValmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const ValmaKoulutuksenOsanSuoritus = (o: {
  arviointi?: Array<TelmaJaValmaArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: ValmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'valmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): ValmaKoulutuksenOsanSuoritus => ({
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valmakoulutuksenosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

ValmaKoulutuksenOsanSuoritus.className =
  'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus' as const

export const isValmaKoulutuksenOsanSuoritus = (
  a: any
): a is ValmaKoulutuksenOsanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus'
