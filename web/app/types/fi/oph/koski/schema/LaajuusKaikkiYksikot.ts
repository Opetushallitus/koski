import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä
 *
 * @see `fi.oph.koski.schema.LaajuusKaikkiYksiköt`
 */
export type LaajuusKaikkiYksiköt = {
  $class: 'fi.oph.koski.schema.LaajuusKaikkiYksiköt'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', string>
}

export const LaajuusKaikkiYksiköt = (o: {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', string>
}): LaajuusKaikkiYksiköt => ({
  $class: 'fi.oph.koski.schema.LaajuusKaikkiYksiköt',
  ...o
})

export const isLaajuusKaikkiYksiköt = (a: any): a is LaajuusKaikkiYksiköt =>
  a?.$class === 'fi.oph.koski.schema.LaajuusKaikkiYksiköt'
