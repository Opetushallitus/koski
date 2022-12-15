import { Lukukausi_Ilmoittautumisjakso } from './LukukausiIlmoittautumisjakso'

/**
 * Lukukausi_Ilmoittautuminen
 *
 * @see `fi.oph.koski.schema.Lukukausi_Ilmoittautuminen`
 */
export type Lukukausi_Ilmoittautuminen = {
  $class: 'fi.oph.koski.schema.Lukukausi_Ilmoittautuminen'
  ilmoittautumisjaksot: Array<Lukukausi_Ilmoittautumisjakso>
}

export const Lukukausi_Ilmoittautuminen = (
  o: {
    ilmoittautumisjaksot?: Array<Lukukausi_Ilmoittautumisjakso>
  } = {}
): Lukukausi_Ilmoittautuminen => ({
  $class: 'fi.oph.koski.schema.Lukukausi_Ilmoittautuminen',
  ilmoittautumisjaksot: [],
  ...o
})

export const isLukukausi_Ilmoittautuminen = (
  a: any
): a is Lukukausi_Ilmoittautuminen => a?.$class === 'Lukukausi_Ilmoittautuminen'
