import { TelmaJaValmaArviointi } from './TelmaJaValmaArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { ValmaKoulutuksenOsa } from './ValmaKoulutuksenOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'

/**
 * Suoritettavan VALMA-koulutuksen osan / osien tiedot
 *
 * @see `fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus`
 */
export type ValmaKoulutuksenOsanSuoritus = {
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus'
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: ValmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export const ValmaKoulutuksenOsanSuoritus = (o: {
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'valmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: ValmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}): ValmaKoulutuksenOsanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valmakoulutuksenosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenOsanSuoritus',
  ...o
})

export const isValmaKoulutuksenOsanSuoritus = (
  a: any
): a is ValmaKoulutuksenOsanSuoritus =>
  a?.$class === 'ValmaKoulutuksenOsanSuoritus'
