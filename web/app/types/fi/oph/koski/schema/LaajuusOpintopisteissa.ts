import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusOpintopisteissä`
 */
export type LaajuusOpintopisteissä = {
  $class: 'fi.oph.koski.schema.LaajuusOpintopisteissä'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '2'>
}

export const LaajuusOpintopisteissä = (o: {
  arvo: number
  yksikkö?: Koodistokoodiviite<'opintojenlaajuusyksikko', '2'>
}): LaajuusOpintopisteissä => ({
  $class: 'fi.oph.koski.schema.LaajuusOpintopisteissä',
  yksikkö: Koodistokoodiviite({
    koodiarvo: '2',
    koodistoUri: 'opintojenlaajuusyksikko'
  }),
  ...o
})

export const isLaajuusOpintopisteissä = (a: any): a is LaajuusOpintopisteissä =>
  a?.$class === 'fi.oph.koski.schema.LaajuusOpintopisteissä'
