import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DeprecatedEBOppiaineKomponentti
 *
 * @see `fi.oph.koski.schema.DeprecatedEBOppiaineKomponentti`
 */
export type DeprecatedEBOppiaineKomponentti = {
  $class: 'fi.oph.koski.schema.DeprecatedEBOppiaineKomponentti'
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}

export const DeprecatedEBOppiaineKomponentti = (o: {
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}): DeprecatedEBOppiaineKomponentti => ({
  $class: 'fi.oph.koski.schema.DeprecatedEBOppiaineKomponentti',
  ...o
})

DeprecatedEBOppiaineKomponentti.className =
  'fi.oph.koski.schema.DeprecatedEBOppiaineKomponentti' as const

export const isDeprecatedEBOppiaineKomponentti = (
  a: any
): a is DeprecatedEBOppiaineKomponentti =>
  a?.$class === 'fi.oph.koski.schema.DeprecatedEBOppiaineKomponentti'
