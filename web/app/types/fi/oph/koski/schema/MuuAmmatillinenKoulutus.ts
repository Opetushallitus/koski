import {
  AmmatilliseenTehtäväänValmistavaKoulutus,
  isAmmatilliseenTehtäväänValmistavaKoulutus
} from './AmmatilliseenTehtavaanValmistavaKoulutus'
import {
  PaikallinenMuuAmmatillinenKoulutus,
  isPaikallinenMuuAmmatillinenKoulutus
} from './PaikallinenMuuAmmatillinenKoulutus'

/**
 * MuuAmmatillinenKoulutus
 *
 * @see `fi.oph.koski.schema.MuuAmmatillinenKoulutus`
 */
export type MuuAmmatillinenKoulutus =
  | AmmatilliseenTehtäväänValmistavaKoulutus
  | PaikallinenMuuAmmatillinenKoulutus

export const isMuuAmmatillinenKoulutus = (
  a: any
): a is MuuAmmatillinenKoulutus =>
  isAmmatilliseenTehtäväänValmistavaKoulutus(a) ||
  isPaikallinenMuuAmmatillinenKoulutus(a)
