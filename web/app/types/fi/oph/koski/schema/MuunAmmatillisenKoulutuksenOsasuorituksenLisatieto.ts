import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa.
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto`
 */
export type MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto'
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}

export const MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto = (o: {
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}): MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto => ({
  $class:
    'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto',
  ...o
})

MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto.className =
  'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto' as const

export const isMuunAmmatillisenKoulutuksenOsasuorituksenLisätieto = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto =>
  a?.$class ===
  'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto'
