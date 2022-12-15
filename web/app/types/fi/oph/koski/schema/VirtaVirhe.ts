import { Duplikaatti, isDuplikaatti } from './Duplikaatti'
import {
  OpiskeluoikeusAvaintaEiLöydy,
  isOpiskeluoikeusAvaintaEiLöydy
} from './OpiskeluoikeusAvaintaEiLoydy'

/**
 * VirtaVirhe
 *
 * @see `fi.oph.koski.schema.VirtaVirhe`
 */
export type VirtaVirhe = Duplikaatti | OpiskeluoikeusAvaintaEiLöydy

export const isVirtaVirhe = (a: any): a is VirtaVirhe =>
  isDuplikaatti(a) || isOpiskeluoikeusAvaintaEiLöydy(a)
