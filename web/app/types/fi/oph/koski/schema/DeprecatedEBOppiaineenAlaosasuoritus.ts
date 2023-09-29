import { DeprecatedEBOppiaineKomponentti } from './DeprecatedEBOppiaineKomponentti'
import { DeprecatedEBArviointi } from './DeprecatedEBArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DeprecatedEBOppiaineenAlaosasuoritus
 *
 * @see `fi.oph.koski.schema.DeprecatedEBOppiaineenAlaosasuoritus`
 */
export type DeprecatedEBOppiaineenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.DeprecatedEBOppiaineenAlaosasuoritus'
  koulutusmoduuli: DeprecatedEBOppiaineKomponentti
  arviointi?: Array<DeprecatedEBArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const DeprecatedEBOppiaineenAlaosasuoritus = (o: {
  koulutusmoduuli: DeprecatedEBOppiaineKomponentti
  arviointi?: Array<DeprecatedEBArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): DeprecatedEBOppiaineenAlaosasuoritus => ({
  $class: 'fi.oph.koski.schema.DeprecatedEBOppiaineenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinnonalaosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

DeprecatedEBOppiaineenAlaosasuoritus.className =
  'fi.oph.koski.schema.DeprecatedEBOppiaineenAlaosasuoritus' as const

export const isDeprecatedEBOppiaineenAlaosasuoritus = (
  a: any
): a is DeprecatedEBOppiaineenAlaosasuoritus =>
  a?.$class === 'fi.oph.koski.schema.DeprecatedEBOppiaineenAlaosasuoritus'
