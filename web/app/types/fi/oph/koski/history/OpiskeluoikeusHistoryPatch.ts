/**
 * OpiskeluoikeusHistoryPatch
 *
 * @see `fi.oph.koski.history.OpiskeluoikeusHistoryPatch`
 */
export type OpiskeluoikeusHistoryPatch = {
  $class: 'fi.oph.koski.history.OpiskeluoikeusHistoryPatch'
  opiskeluoikeusOid: string
  versionumero: number
  aikaleima: string
  muutos: any
  kayttajaOid: string
}

export const OpiskeluoikeusHistoryPatch = (o: {
  opiskeluoikeusOid: string
  versionumero: number
  aikaleima: string
  muutos: any
  kayttajaOid: string
}): OpiskeluoikeusHistoryPatch => ({
  $class: 'fi.oph.koski.history.OpiskeluoikeusHistoryPatch',
  ...o
})

OpiskeluoikeusHistoryPatch.className =
  'fi.oph.koski.history.OpiskeluoikeusHistoryPatch' as const

export const isOpiskeluoikeusHistoryPatch = (
  a: any
): a is OpiskeluoikeusHistoryPatch =>
  a?.$class === 'fi.oph.koski.history.OpiskeluoikeusHistoryPatch'
