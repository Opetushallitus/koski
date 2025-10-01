import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * Paikallisten muun ammatillisen koulutuksen opiskeluoikeuksien siirtymäaika päättyy 31.8.2028. Siirtymäajan jälkeen alkavien uusien opiskeluoikeuksien tallentaminen on estetty.
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

PaikallinenMuuAmmatillinenKoulutus.className =
  'fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus' as const

export const isPaikallinenMuuAmmatillinenKoulutus = (
  a: any
): a is PaikallinenMuuAmmatillinenKoulutus =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenMuuAmmatillinenKoulutus'
