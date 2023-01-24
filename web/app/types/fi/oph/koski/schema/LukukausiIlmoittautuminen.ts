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

Lukukausi_Ilmoittautuminen.className =
  'fi.oph.koski.schema.Lukukausi_Ilmoittautuminen' as const

export const isLukukausi_Ilmoittautuminen = (
  a: any
): a is Lukukausi_Ilmoittautuminen =>
  a?.$class === 'fi.oph.koski.schema.Lukukausi_Ilmoittautuminen'
