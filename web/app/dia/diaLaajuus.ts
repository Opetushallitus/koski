import { config } from '../util/config'

// DIA-laajuus opintopisteinä 1.8.2026 alkaen, ennen vuosiviikkotunteina.
export const diaLaajuusOpintopisteinä = (alkamispäivä?: string): boolean =>
  typeof alkamispäivä === 'string' &&
  alkamispäivä >= config().rajapäivät.diaLaajuusOpintopisteinäAlkaen
