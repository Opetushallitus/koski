import { DIAOppiaineenTutkintovaiheenOsasuoritus } from './DIAOppiaineenTutkintovaiheenOsasuoritus'
import { DIATutkintovaiheenArviointi } from './DIATutkintovaiheenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus`
 */
export type DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus
  arviointi?: Array<DIATutkintovaiheenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = (o: {
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus
  arviointi?: Array<DIATutkintovaiheenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus => ({
  $class:
    'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diaoppiaineentutkintovaiheenosasuorituksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'
