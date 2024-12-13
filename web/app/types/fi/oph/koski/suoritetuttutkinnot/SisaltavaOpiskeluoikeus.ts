import { Oppilaitos } from './Oppilaitos'

/**
 * SisältäväOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SisältäväOpiskeluoikeus`
 */
export type SisältäväOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SisältäväOpiskeluoikeus'
  oid: string
  oppilaitos: Oppilaitos
}

export const SisältäväOpiskeluoikeus = (o: {
  oid: string
  oppilaitos: Oppilaitos
}): SisältäväOpiskeluoikeus => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.SisältäväOpiskeluoikeus',
  ...o
})

SisältäväOpiskeluoikeus.className =
  'fi.oph.koski.suoritetuttutkinnot.SisältäväOpiskeluoikeus' as const

export const isSisältäväOpiskeluoikeus = (
  a: any
): a is SisältäväOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.SisältäväOpiskeluoikeus'
