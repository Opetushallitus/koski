import {
  LaajuusOpintopisteissä,
  isLaajuusOpintopisteissä
} from './LaajuusOpintopisteissa'
import { LaajuusTunneissa, isLaajuusTunneissa } from './LaajuusTunneissa'

/**
 * LaajuusOpintopisteissäTaiTunneissa
 *
 * @see `fi.oph.koski.schema.LaajuusOpintopisteissäTaiTunneissa`
 */
export type LaajuusOpintopisteissäTaiTunneissa =
  | LaajuusOpintopisteissä
  | LaajuusTunneissa

export const isLaajuusOpintopisteissäTaiTunneissa = (
  a: any
): a is LaajuusOpintopisteissäTaiTunneissa =>
  isLaajuusOpintopisteissä(a) || isLaajuusTunneissa(a)
