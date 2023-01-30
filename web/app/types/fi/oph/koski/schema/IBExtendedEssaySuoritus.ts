import { IBCoreRequirementsArviointi } from './IBCoreRequirementsArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBOppiaineExtendedEssay } from './IBOppiaineExtendedEssay'

/**
 * Extended Essay-suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.IBExtendedEssaySuoritus`
 */
export type IBExtendedEssaySuoritus = {
  $class: 'fi.oph.koski.schema.IBExtendedEssaySuoritus'
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaineee'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineExtendedEssay
}

export const IBExtendedEssaySuoritus = (o: {
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaineee'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineExtendedEssay
}): IBExtendedEssaySuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'iboppiaineee',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBExtendedEssaySuoritus',
  ...o
})

IBExtendedEssaySuoritus.className =
  'fi.oph.koski.schema.IBExtendedEssaySuoritus' as const

export const isIBExtendedEssaySuoritus = (
  a: any
): a is IBExtendedEssaySuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBExtendedEssaySuoritus'
