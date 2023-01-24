import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'

/**
 * Suorituksen vahvistus organisaatiotiedoilla
 *
 * @see `fi.oph.koski.schema.Organisaatiovahvistus`
 */
export type Organisaatiovahvistus = {
  $class: 'fi.oph.koski.schema.Organisaatiovahvistus'
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
}

export const Organisaatiovahvistus = (o: {
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
}): Organisaatiovahvistus => ({
  $class: 'fi.oph.koski.schema.Organisaatiovahvistus',
  ...o
})

Organisaatiovahvistus.className =
  'fi.oph.koski.schema.Organisaatiovahvistus' as const

export const isOrganisaatiovahvistus = (a: any): a is Organisaatiovahvistus =>
  a?.$class === 'fi.oph.koski.schema.Organisaatiovahvistus'
