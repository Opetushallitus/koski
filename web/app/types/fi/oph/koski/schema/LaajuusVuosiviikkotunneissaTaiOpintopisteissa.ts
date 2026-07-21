import {
  LaajuusOpintopisteissä,
  isLaajuusOpintopisteissä
} from './LaajuusOpintopisteissa'
import {
  LaajuusVuosiviikkotunneissa,
  isLaajuusVuosiviikkotunneissa
} from './LaajuusVuosiviikkotunneissa'

/**
 * LaajuusVuosiviikkotunneissaTaiOpintopisteissä
 *
 * @see `fi.oph.koski.schema.LaajuusVuosiviikkotunneissaTaiOpintopisteissä`
 */
export type LaajuusVuosiviikkotunneissaTaiOpintopisteissä =
  LaajuusOpintopisteissä | LaajuusVuosiviikkotunneissa

export const isLaajuusVuosiviikkotunneissaTaiOpintopisteissä = (
  a: any
): a is LaajuusVuosiviikkotunneissaTaiOpintopisteissä =>
  isLaajuusOpintopisteissä(a) || isLaajuusVuosiviikkotunneissa(a)
