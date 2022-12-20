import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusTunneissa`
 */
export type LaajuusTunneissa = {
  $class: 'fi.oph.koski.schema.LaajuusTunneissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '5'>
}

export const LaajuusTunneissa = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '5'>
}): LaajuusTunneissa => ({
  $class: 'fi.oph.koski.schema.LaajuusTunneissa',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '5',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusTunneissa = (a: any): a is LaajuusTunneissa =>
  a?.$class === 'fi.oph.koski.schema.LaajuusTunneissa'
