import { InternationalSchoolCoreRequirementsArviointi } from './InternationalSchoolCoreRequirementsArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DiplomaCoreRequirementsOppiaine } from './DiplomaCoreRequirementsOppiaine'

/**
 * DiplomaCoreRequirementsOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.DiplomaCoreRequirementsOppiaineenSuoritus`
 */
export type DiplomaCoreRequirementsOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaineenSuoritus'
  arviointi?: Array<InternationalSchoolCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolcorerequirements'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DiplomaCoreRequirementsOppiaine
}

export const DiplomaCoreRequirementsOppiaineenSuoritus = (o: {
  arviointi?: Array<InternationalSchoolCoreRequirementsArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolcorerequirements'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DiplomaCoreRequirementsOppiaine
}): DiplomaCoreRequirementsOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschoolcorerequirements',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaineenSuoritus',
  ...o
})

DiplomaCoreRequirementsOppiaineenSuoritus.className =
  'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaineenSuoritus' as const

export const isDiplomaCoreRequirementsOppiaineenSuoritus = (
  a: any
): a is DiplomaCoreRequirementsOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaineenSuoritus'
