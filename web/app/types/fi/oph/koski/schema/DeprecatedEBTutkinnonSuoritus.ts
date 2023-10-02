import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { EBTutkinto } from './EBTutkinto'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { DeprecatedEBTutkinnonOsasuoritus } from './DeprecatedEBTutkinnonOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * DeprecatedEBTutkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.DeprecatedEBTutkinnonSuoritus`
 */
export type DeprecatedEBTutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  yleisarvosana?: number
  koulutusmoduuli: EBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DeprecatedEBTutkinnonOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const DeprecatedEBTutkinnonSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  yleisarvosana?: number
  koulutusmoduuli: EBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DeprecatedEBTutkinnonOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): DeprecatedEBTutkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkinnonSuoritus',
  ...o
})

DeprecatedEBTutkinnonSuoritus.className =
  'fi.oph.koski.schema.DeprecatedEBTutkinnonSuoritus' as const

export const isDeprecatedEBTutkinnonSuoritus = (
  a: any
): a is DeprecatedEBTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DeprecatedEBTutkinnonSuoritus'
