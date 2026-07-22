import { DIATutkintovaiheenArviointi } from './DIATutkintovaiheenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAOppiaineenTutkintovaiheenOsasuoritus } from './DIAOppiaineenTutkintovaiheenOsasuoritus'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus`
 */
export type DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'
  arviointi?: Array<DIATutkintovaiheenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus
  tunnustettu?: OsaamisenTunnustaminen
}

export const DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = (o: {
  arviointi?: Array<DIATutkintovaiheenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus
  tunnustettu?: OsaamisenTunnustaminen
}): DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diaoppiaineentutkintovaiheenosasuorituksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus',
  ...o
})

DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus.className =
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus' as const

export const isDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'
