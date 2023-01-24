import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'

/**
 * GroupedKoodistot
 *
 * @see `fi.oph.koski.typemodel.GroupedKoodistot`
 */
export type GroupedKoodistot = {
  $class: 'fi.oph.koski.typemodel.GroupedKoodistot'
  koodistot: Record<string, Array<Koodistokoodiviite>>
}

export const GroupedKoodistot = (
  o: {
    koodistot?: Record<string, Array<Koodistokoodiviite>>
  } = {}
): GroupedKoodistot => ({
  $class: 'fi.oph.koski.typemodel.GroupedKoodistot',
  koodistot: {},
  ...o
})

GroupedKoodistot.className = 'fi.oph.koski.typemodel.GroupedKoodistot' as const

export const isGroupedKoodistot = (a: any): a is GroupedKoodistot =>
  a?.$class === 'fi.oph.koski.typemodel.GroupedKoodistot'
