import { LaajuusKursseissa, isLaajuusKursseissa } from './LaajuusKursseissa'
import {
  LaajuusOpintopisteissä,
  isLaajuusOpintopisteissä
} from './LaajuusOpintopisteissa'

/**
 * LaajuusOpintopisteissäTaiKursseissa
 *
 * @see `fi.oph.koski.schema.LaajuusOpintopisteissäTaiKursseissa`
 */
export type LaajuusOpintopisteissäTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusOpintopisteissä

export const isLaajuusOpintopisteissäTaiKursseissa = (
  a: any
): a is LaajuusOpintopisteissäTaiKursseissa =>
  isLaajuusKursseissa(a) || isLaajuusOpintopisteissä(a)
