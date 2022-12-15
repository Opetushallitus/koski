import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * MuunAmmatillisenKoulutuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuoritus`
 */
export type MuunAmmatillisenKoulutuksenOsasuoritus = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuoritus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export const MuunAmmatillisenKoulutuksenOsasuoritus = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}): MuunAmmatillisenKoulutuksenOsasuoritus => ({
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenOsasuoritus',
  ...o
})

export const isMuunAmmatillisenKoulutuksenOsasuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuoritus =>
  a?.$class === 'MuunAmmatillisenKoulutuksenOsasuoritus'
