import { config } from '../util/config'

// DIA-tutkinnon laajuus ilmoitetaan opintopisteinä 1.8.2026 tai myöhemmin alkaneille
// opiskeluoikeuksille, sitä ennen vuosiviikkotunteina. Yksikkö määräytyy siis
// opiskeluoikeuden alkamispäivän perusteella.
export const diaLaajuusOpintopisteinä = (alkamispäivä?: string): boolean =>
  typeof alkamispäivä === 'string' &&
  alkamispäivä >= config().rajapäivät.diaLaajuusOpintopisteinäAlkaen
