import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ammatillisen tutkinnon tunnistetiedot. Ammatillisille koulutuksille on ePerusteet
 *
 * @see `fi.oph.koski.schema.AmmatillinenTutkintoKoulutus`
 */
export type AmmatillinenTutkintoKoulutus = {
  $class: 'fi.oph.koski.schema.AmmatillinenTutkintoKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', string>
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const AmmatillinenTutkintoKoulutus = (o: {
  tunniste: Koodistokoodiviite<'koulutus', string>
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}): AmmatillinenTutkintoKoulutus => ({
  $class: 'fi.oph.koski.schema.AmmatillinenTutkintoKoulutus',
  ...o
})

export const isAmmatillinenTutkintoKoulutus = (
  a: any
): a is AmmatillinenTutkintoKoulutus =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenTutkintoKoulutus'
