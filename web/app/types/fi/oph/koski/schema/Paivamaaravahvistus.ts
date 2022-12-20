import { Organisaatio } from './Organisaatio'

/**
 * Päivämäärävahvistus
 *
 * @see `fi.oph.koski.schema.Päivämäärävahvistus`
 */
export type Päivämäärävahvistus = {
  $class: 'fi.oph.koski.schema.Päivämäärävahvistus'
  päivä: string
  myöntäjäOrganisaatio: Organisaatio
}

export const Päivämäärävahvistus = (o: {
  päivä: string
  myöntäjäOrganisaatio: Organisaatio
}): Päivämäärävahvistus => ({
  $class: 'fi.oph.koski.schema.Päivämäärävahvistus',
  ...o
})

export const isPäivämäärävahvistus = (a: any): a is Päivämäärävahvistus =>
  a?.$class === 'fi.oph.koski.schema.Päivämäärävahvistus'
