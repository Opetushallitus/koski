import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Vapaatavoitteisen vapaan sivistystyön koulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteinenKoulutus`
 */
export type VapaanSivistystyönVapaatavoitteinenKoulutus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteinenKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export const VapaanSivistystyönVapaatavoitteinenKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '099999'>
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    laajuus?: LaajuusOpintopisteissä
    opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet', string>
  } = {}
): VapaanSivistystyönVapaatavoitteinenKoulutus => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteinenKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '099999',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isVapaanSivistystyönVapaatavoitteinenKoulutus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteinenKoulutus =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteinenKoulutus'
