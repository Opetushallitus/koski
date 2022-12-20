import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönLukutaidonKokonaisuus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönLukutaidonKokonaisuus`
 */
export type VapaanSivistystyönLukutaidonKokonaisuus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaidonKokonaisuus'
  tunniste: Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export const VapaanSivistystyönLukutaidonKokonaisuus = (o: {
  tunniste: Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}): VapaanSivistystyönLukutaidonKokonaisuus => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaidonKokonaisuus',
  ...o
})

export const isVapaanSivistystyönLukutaidonKokonaisuus = (
  a: any
): a is VapaanSivistystyönLukutaidonKokonaisuus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönLukutaidonKokonaisuus'
