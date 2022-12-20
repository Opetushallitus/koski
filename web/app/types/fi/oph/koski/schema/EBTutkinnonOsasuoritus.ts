import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SecondaryOppiaine } from './SecondaryOppiaine'
import { EBOppiaineenAlaosasuoritus } from './EBOppiaineenAlaosasuoritus'

/**
 * EBTutkinnonOsasuoritus
 *
 * @see `fi.oph.koski.schema.EBTutkinnonOsasuoritus`
 */
export type EBTutkinnonOsasuoritus = {
  $class: 'fi.oph.koski.schema.EBTutkinnonOsasuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<EBOppiaineenAlaosasuoritus>
}

export const EBTutkinnonOsasuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<EBOppiaineenAlaosasuoritus>
}): EBTutkinnonOsasuoritus => ({
  $class: 'fi.oph.koski.schema.EBTutkinnonOsasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinnonosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isEBTutkinnonOsasuoritus = (a: any): a is EBTutkinnonOsasuoritus =>
  a?.$class === 'fi.oph.koski.schema.EBTutkinnonOsasuoritus'
