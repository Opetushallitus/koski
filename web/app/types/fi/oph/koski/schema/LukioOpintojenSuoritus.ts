import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { PaikallinenLukionOpinto } from './PaikallinenLukionOpinto'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * LukioOpintojenSuoritus
 *
 * @see `fi.oph.koski.schema.LukioOpintojenSuoritus`
 */
export type LukioOpintojenSuoritus = {
  $class: 'fi.oph.koski.schema.LukioOpintojenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinenlukionopintoja'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenLukionOpinto
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukioOpintojenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinenlukionopintoja'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenLukionOpinto
  tunnustettu?: OsaamisenTunnustaminen
}): LukioOpintojenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenlukionopintoja',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukioOpintojenSuoritus',
  ...o
})

LukioOpintojenSuoritus.className =
  'fi.oph.koski.schema.LukioOpintojenSuoritus' as const

export const isLukioOpintojenSuoritus = (a: any): a is LukioOpintojenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.LukioOpintojenSuoritus'
