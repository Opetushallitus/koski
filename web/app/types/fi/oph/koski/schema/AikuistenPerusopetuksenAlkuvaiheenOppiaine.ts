import {
  AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine,
  isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine
} from './AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'
import {
  AikuistenPerusopetuksenAlkuvaiheenVierasKieli,
  isAikuistenPerusopetuksenAlkuvaiheenVierasKieli
} from './AikuistenPerusopetuksenAlkuvaiheenVierasKieli'
import {
  AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus,
  isAikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus
} from './AikuistenPerusopetuksenAlkuvaiheenAidinkieliJaKirjallisuus'
import {
  MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine,
  isMuuAikuistenPerusopetuksenAlkuvaiheenOppiaine
} from './MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine'

/**
 * AikuistenPerusopetuksenAlkuvaiheenOppiaine
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenOppiaine`
 */
export type AikuistenPerusopetuksenAlkuvaiheenOppiaine =
  | AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine
  | AikuistenPerusopetuksenAlkuvaiheenVierasKieli
  | AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus
  | MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine

export const isAikuistenPerusopetuksenAlkuvaiheenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenOppiaine =>
  isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenVierasKieli(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(a) ||
  isMuuAikuistenPerusopetuksenAlkuvaiheenOppiaine(a)
