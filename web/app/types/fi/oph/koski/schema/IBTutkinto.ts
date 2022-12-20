import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-tutkinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBTutkinto`
 */
export type IBTutkinto = {
  $class: 'fi.oph.koski.schema.IBTutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301102'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const IBTutkinto = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '301102'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): IBTutkinto => ({
  $class: 'fi.oph.koski.schema.IBTutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '301102',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isIBTutkinto = (a: any): a is IBTutkinto =>
  a?.$class === 'fi.oph.koski.schema.IBTutkinto'
