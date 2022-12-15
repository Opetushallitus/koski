import { LaajuusKursseissa, isLaajuusKursseissa } from './LaajuusKursseissa'
import {
  LaajuusVuosiviikkotunneissa,
  isLaajuusVuosiviikkotunneissa
} from './LaajuusVuosiviikkotunneissa'

/**
 * LaajuusVuosiviikkotunneissaTaiKursseissa
 *
 * @see `fi.oph.koski.schema.LaajuusVuosiviikkotunneissaTaiKursseissa`
 */
export type LaajuusVuosiviikkotunneissaTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusVuosiviikkotunneissa

export const isLaajuusVuosiviikkotunneissaTaiKursseissa = (
  a: any
): a is LaajuusVuosiviikkotunneissaTaiKursseissa =>
  isLaajuusKursseissa(a) || isLaajuusVuosiviikkotunneissa(a)
