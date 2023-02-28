import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Ylioppilastutkinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.Ylioppilastutkinto`
 */
export type Ylioppilastutkinto = {
  $class: 'fi.oph.koski.schema.Ylioppilastutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301000'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const Ylioppilastutkinto = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '301000'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): Ylioppilastutkinto => ({
  $class: 'fi.oph.koski.schema.Ylioppilastutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '301000',
    koodistoUri: 'koulutus'
  }),
  ...o
})

Ylioppilastutkinto.className = 'fi.oph.koski.schema.Ylioppilastutkinto' as const

export const isYlioppilastutkinto = (a: any): a is Ylioppilastutkinto =>
  a?.$class === 'fi.oph.koski.schema.Ylioppilastutkinto'
