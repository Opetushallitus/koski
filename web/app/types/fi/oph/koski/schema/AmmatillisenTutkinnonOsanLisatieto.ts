import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa.
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsanLisätieto`
 */
export type AmmatillisenTutkinnonOsanLisätieto = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsanLisätieto'
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}

export const AmmatillisenTutkinnonOsanLisätieto = (o: {
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}): AmmatillisenTutkinnonOsanLisätieto => ({
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsanLisätieto',
  ...o
})

export const isAmmatillisenTutkinnonOsanLisätieto = (
  a: any
): a is AmmatillisenTutkinnonOsanLisätieto =>
  a?.$class === 'AmmatillisenTutkinnonOsanLisätieto'
