import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * OsaamisenHankkimistapaIlmanLisätietoja
 *
 * @see `fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja`
 */
export type OsaamisenHankkimistapaIlmanLisätietoja = {
  $class: 'fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja'
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', string>
}

export const OsaamisenHankkimistapaIlmanLisätietoja = (o: {
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', string>
}): OsaamisenHankkimistapaIlmanLisätietoja => ({
  $class: 'fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja',
  ...o
})

OsaamisenHankkimistapaIlmanLisätietoja.className =
  'fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja' as const

export const isOsaamisenHankkimistapaIlmanLisätietoja = (
  a: any
): a is OsaamisenHankkimistapaIlmanLisätietoja =>
  a?.$class === 'fi.oph.koski.schema.OsaamisenHankkimistapaIlmanLisätietoja'
