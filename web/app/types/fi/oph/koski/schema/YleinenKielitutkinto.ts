import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * YleinenKielitutkinto
 *
 * @see `fi.oph.koski.schema.YleinenKielitutkinto`
 */
export type YleinenKielitutkinto = {
  $class: 'fi.oph.koski.schema.YleinenKielitutkinto'
  tunniste: Koodistokoodiviite<'ykitutkintotaso', string>
  kieli: Koodistokoodiviite<
    'kieli',
    'FI' | 'EN' | 'ES' | 'IT' | 'SE' | 'FR' | 'SV' | 'RU' | 'DE'
  >
}

export const YleinenKielitutkinto = (o: {
  tunniste: Koodistokoodiviite<'ykitutkintotaso', string>
  kieli: Koodistokoodiviite<
    'kieli',
    'FI' | 'EN' | 'ES' | 'IT' | 'SE' | 'FR' | 'SV' | 'RU' | 'DE'
  >
}): YleinenKielitutkinto => ({
  $class: 'fi.oph.koski.schema.YleinenKielitutkinto',
  ...o
})

YleinenKielitutkinto.className =
  'fi.oph.koski.schema.YleinenKielitutkinto' as const

export const isYleinenKielitutkinto = (a: any): a is YleinenKielitutkinto =>
  a?.$class === 'fi.oph.koski.schema.YleinenKielitutkinto'
