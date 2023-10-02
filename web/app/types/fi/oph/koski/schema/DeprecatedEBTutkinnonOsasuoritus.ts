import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryOppiaine } from './SecondaryOppiaine'
import { DeprecatedEBOppiaineenAlaosasuoritus } from './DeprecatedEBOppiaineenAlaosasuoritus'

/**
 * DeprecatedEBTutkinnonOsasuoritus
 *
 * @see `fi.oph.koski.schema.DeprecatedEBTutkinnonOsasuoritus`
 */
export type DeprecatedEBTutkinnonOsasuoritus = {
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkinnonOsasuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<DeprecatedEBOppiaineenAlaosasuoritus>
}

export const DeprecatedEBTutkinnonOsasuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<DeprecatedEBOppiaineenAlaosasuoritus>
}): DeprecatedEBTutkinnonOsasuoritus => ({
  $class: 'fi.oph.koski.schema.DeprecatedEBTutkinnonOsasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinnonosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

DeprecatedEBTutkinnonOsasuoritus.className =
  'fi.oph.koski.schema.DeprecatedEBTutkinnonOsasuoritus' as const

export const isDeprecatedEBTutkinnonOsasuoritus = (
  a: any
): a is DeprecatedEBTutkinnonOsasuoritus =>
  a?.$class === 'fi.oph.koski.schema.DeprecatedEBTutkinnonOsasuoritus'
