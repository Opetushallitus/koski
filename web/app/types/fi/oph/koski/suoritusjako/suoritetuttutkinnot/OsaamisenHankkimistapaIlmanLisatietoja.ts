import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * OsaamisenHankkimistapaIlmanLisätietoja
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.OsaamisenHankkimistapaIlmanLisätietoja`
 */
export type OsaamisenHankkimistapaIlmanLisätietoja = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OsaamisenHankkimistapaIlmanLisätietoja'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const OsaamisenHankkimistapaIlmanLisätietoja = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}): OsaamisenHankkimistapaIlmanLisätietoja => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OsaamisenHankkimistapaIlmanLisätietoja',
  ...o
})

OsaamisenHankkimistapaIlmanLisätietoja.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OsaamisenHankkimistapaIlmanLisätietoja' as const

export const isOsaamisenHankkimistapaIlmanLisätietoja = (
  a: any
): a is OsaamisenHankkimistapaIlmanLisätietoja =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OsaamisenHankkimistapaIlmanLisätietoja'
