import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Vapaan sivistystyön lukutaitokoulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutus`
 */
export type VapaanSivistystyönLukutaitokoulutus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999911'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export const VapaanSivistystyönLukutaitokoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999911'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): VapaanSivistystyönLukutaitokoulutus => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999911',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isVapaanSivistystyönLukutaitokoulutus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutus'
