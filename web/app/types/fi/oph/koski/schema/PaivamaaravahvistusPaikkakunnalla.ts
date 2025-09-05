import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'

/**
 * PäivämäärävahvistusPaikkakunnalla
 *
 * @see `fi.oph.koski.schema.PäivämäärävahvistusPaikkakunnalla`
 */
export type PäivämäärävahvistusPaikkakunnalla = {
  $class: 'fi.oph.koski.schema.PäivämäärävahvistusPaikkakunnalla'
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
}

export const PäivämäärävahvistusPaikkakunnalla = (o: {
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
}): PäivämäärävahvistusPaikkakunnalla => ({
  $class: 'fi.oph.koski.schema.PäivämäärävahvistusPaikkakunnalla',
  ...o
})

PäivämäärävahvistusPaikkakunnalla.className =
  'fi.oph.koski.schema.PäivämäärävahvistusPaikkakunnalla' as const

export const isPäivämäärävahvistusPaikkakunnalla = (
  a: any
): a is PäivämäärävahvistusPaikkakunnalla =>
  a?.$class === 'fi.oph.koski.schema.PäivämäärävahvistusPaikkakunnalla'
