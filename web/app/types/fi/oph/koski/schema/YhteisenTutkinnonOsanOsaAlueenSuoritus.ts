import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { AmmatillisenTutkinnonOsanOsaAlue } from './AmmatillisenTutkinnonOsanOsaAlue'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { Näyttö } from './Naytto'

/**
 * Yhteisen tutkinnon osan osa-alueen suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus`
 */
export type YhteisenTutkinnonOsanOsaAlueenSuoritus = {
  $class: 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue
  tunnustettu?: OsaamisenTunnustaminen
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosanosaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const YhteisenTutkinnonOsanOsaAlueenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue
  tunnustettu?: OsaamisenTunnustaminen
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosanosaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): YhteisenTutkinnonOsanOsaAlueenSuoritus => ({
  $class: 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosanosaalue',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

YhteisenTutkinnonOsanOsaAlueenSuoritus.className =
  'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus' as const

export const isYhteisenTutkinnonOsanOsaAlueenSuoritus = (
  a: any
): a is YhteisenTutkinnonOsanOsaAlueenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus'
