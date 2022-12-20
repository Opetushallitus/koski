import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * PaikallinenMuuAmmatillinenKoulutus
 *
 * @see `fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus`
 */
export type PaikallinenMuuAmmatillinenKoulutus = {
  $class: 'fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export const PaikallinenMuuAmmatillinenKoulutus = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}): PaikallinenMuuAmmatillinenKoulutus => ({
  $class: 'fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus',
  ...o
})

export const isPaikallinenMuuAmmatillinenKoulutus = (
  a: any
): a is PaikallinenMuuAmmatillinenKoulutus =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus'
