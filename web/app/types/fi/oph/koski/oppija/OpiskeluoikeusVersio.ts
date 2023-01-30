import { LähdejärjestelmäId } from '../schema/LahdejarjestelmaId'

/**
 * OpiskeluoikeusVersio
 *
 * @see `fi.oph.koski.oppija.OpiskeluoikeusVersio`
 */
export type OpiskeluoikeusVersio = {
  $class: 'fi.oph.koski.oppija.OpiskeluoikeusVersio'
  oid: string
  versionumero: number
  lähdejärjestelmänId?: LähdejärjestelmäId
}

export const OpiskeluoikeusVersio = (o: {
  oid: string
  versionumero: number
  lähdejärjestelmänId?: LähdejärjestelmäId
}): OpiskeluoikeusVersio => ({
  $class: 'fi.oph.koski.oppija.OpiskeluoikeusVersio',
  ...o
})

OpiskeluoikeusVersio.className =
  'fi.oph.koski.oppija.OpiskeluoikeusVersio' as const

export const isOpiskeluoikeusVersio = (a: any): a is OpiskeluoikeusVersio =>
  a?.$class === 'fi.oph.koski.oppija.OpiskeluoikeusVersio'
