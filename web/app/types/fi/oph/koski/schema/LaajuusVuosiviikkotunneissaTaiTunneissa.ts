import { LaajuusTunneissa, isLaajuusTunneissa } from './LaajuusTunneissa'
import {
  LaajuusVuosiviikkotunneissa,
  isLaajuusVuosiviikkotunneissa
} from './LaajuusVuosiviikkotunneissa'

/**
 * LaajuusVuosiviikkotunneissaTaiTunneissa
 *
 * @see `fi.oph.koski.schema.LaajuusVuosiviikkotunneissaTaiTunneissa`
 */
export type LaajuusVuosiviikkotunneissaTaiTunneissa =
  | LaajuusTunneissa
  | LaajuusVuosiviikkotunneissa

export const isLaajuusVuosiviikkotunneissaTaiTunneissa = (
  a: any
): a is LaajuusVuosiviikkotunneissaTaiTunneissa =>
  isLaajuusTunneissa(a) || isLaajuusVuosiviikkotunneissa(a)
