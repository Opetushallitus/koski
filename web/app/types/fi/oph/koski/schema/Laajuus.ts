import {
  LaajuusKaikkiYksiköt,
  isLaajuusKaikkiYksiköt
} from './LaajuusKaikkiYksikot'
import { LaajuusKursseissa, isLaajuusKursseissa } from './LaajuusKursseissa'
import {
  LaajuusOpintopisteissä,
  isLaajuusOpintopisteissä
} from './LaajuusOpintopisteissa'
import {
  LaajuusOpintoviikoissa,
  isLaajuusOpintoviikoissa
} from './LaajuusOpintoviikoissa'
import {
  LaajuusOsaamispisteissä,
  isLaajuusOsaamispisteissä
} from './LaajuusOsaamispisteissa'
import { LaajuusTunneissa, isLaajuusTunneissa } from './LaajuusTunneissa'
import { LaajuusViikoissa, isLaajuusViikoissa } from './LaajuusViikoissa'
import {
  LaajuusVuosiviikkotunneissa,
  isLaajuusVuosiviikkotunneissa
} from './LaajuusVuosiviikkotunneissa'

/**
 * Laajuus
 *
 * @see `fi.oph.koski.schema.Laajuus`
 */
export type Laajuus =
  | LaajuusKaikkiYksiköt
  | LaajuusKursseissa
  | LaajuusOpintopisteissä
  | LaajuusOpintoviikoissa
  | LaajuusOsaamispisteissä
  | LaajuusTunneissa
  | LaajuusViikoissa
  | LaajuusVuosiviikkotunneissa

export const isLaajuus = (a: any): a is Laajuus =>
  isLaajuusKaikkiYksiköt(a) ||
  isLaajuusKursseissa(a) ||
  isLaajuusOpintopisteissä(a) ||
  isLaajuusOpintoviikoissa(a) ||
  isLaajuusOsaamispisteissä(a) ||
  isLaajuusTunneissa(a) ||
  isLaajuusViikoissa(a) ||
  isLaajuusVuosiviikkotunneissa(a)
