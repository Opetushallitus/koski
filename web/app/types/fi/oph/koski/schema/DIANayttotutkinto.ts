import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIA-tutkinnon erityisosaamisen näyttötutkinnon tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIANäyttötutkinto`
 */
export type DIANäyttötutkinto = {
  $class: 'fi.oph.koski.schema.DIANäyttötutkinto'
  tunniste: Koodistokoodiviite<'diapaattokoe', 'nayttotutkinto'>
}

export const DIANäyttötutkinto = (
  o: {
    tunniste?: Koodistokoodiviite<'diapaattokoe', 'nayttotutkinto'>
  } = {}
): DIANäyttötutkinto => ({
  $class: 'fi.oph.koski.schema.DIANäyttötutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'nayttotutkinto',
    koodistoUri: 'diapaattokoe'
  }),
  ...o
})

export const isDIANäyttötutkinto = (a: any): a is DIANäyttötutkinto =>
  a?.$class === 'fi.oph.koski.schema.DIANäyttötutkinto'
