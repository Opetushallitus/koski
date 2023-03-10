import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Näyttö } from './Naytto'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { AmmatillisenTutkinnonOsanOsaAlue } from './AmmatillisenTutkinnonOsanOsaAlue'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Yhteisen tutkinnon osan osa-alueen suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus`
 */
export type YhteisenTutkinnonOsanOsaAlueenSuoritus = {
  $class: 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosanosaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue
  tunnustettu?: OsaamisenTunnustaminen
}

export const YhteisenTutkinnonOsanOsaAlueenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosanosaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  korotettu?: Koodistokoodiviite<'ammatillisensuorituksenkorotus', string>
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue
  tunnustettu?: OsaamisenTunnustaminen
}): YhteisenTutkinnonOsanOsaAlueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillisentutkinnonosanosaalue',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus',
  ...o
})

YhteisenTutkinnonOsanOsaAlueenSuoritus.className =
  'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus' as const

export const isYhteisenTutkinnonOsanOsaAlueenSuoritus = (
  a: any
): a is YhteisenTutkinnonOsanOsaAlueenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.YhteisenTutkinnonOsanOsaAlueenSuoritus'
