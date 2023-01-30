import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIA-tutkintovaiheen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIATutkinto`
 */
export type DIATutkinto = {
  $class: 'fi.oph.koski.schema.DIATutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301103'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const DIATutkinto = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '301103'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): DIATutkinto => ({
  $class: 'fi.oph.koski.schema.DIATutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '301103',
    koodistoUri: 'koulutus'
  }),
  ...o
})

DIATutkinto.className = 'fi.oph.koski.schema.DIATutkinto' as const

export const isDIATutkinto = (a: any): a is DIATutkinto =>
  a?.$class === 'fi.oph.koski.schema.DIATutkinto'
