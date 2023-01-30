import { TelmaJaValmaArviointi } from './TelmaJaValmaArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { TelmaKoulutuksenOsa } from './TelmaKoulutuksenOsa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { HenkilövahvistusValinnaisellaTittelillä } from './HenkilovahvistusValinnaisellaTittelilla'

/**
 * Suoritettavan TELMA-koulutuksen osan tiedot
 *
 * @see `fi.oph.koski.schema.TelmaKoulutuksenOsanSuoritus`
 */
export type TelmaKoulutuksenOsanSuoritus = {
  $class: 'fi.oph.koski.schema.TelmaKoulutuksenOsanSuoritus'
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'telmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: TelmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export const TelmaKoulutuksenOsanSuoritus = (o: {
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'telmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: TelmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}): TelmaKoulutuksenOsanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'telmakoulutuksenosa',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.TelmaKoulutuksenOsanSuoritus',
  ...o
})

TelmaKoulutuksenOsanSuoritus.className =
  'fi.oph.koski.schema.TelmaKoulutuksenOsanSuoritus' as const

export const isTelmaKoulutuksenOsanSuoritus = (
  a: any
): a is TelmaKoulutuksenOsanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.TelmaKoulutuksenOsanSuoritus'
