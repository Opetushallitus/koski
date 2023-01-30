import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * VapaanSivistystyönJotpaKoulutus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus`
 */
export type VapaanSivistystyönJotpaKoulutus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export const VapaanSivistystyönJotpaKoulutus = (o: {
  tunniste?: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}): VapaanSivistystyönJotpaKoulutus => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '099999',
    koodistoUri: 'koulutus'
  }),
  ...o
})

VapaanSivistystyönJotpaKoulutus.className =
  'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus' as const

export const isVapaanSivistystyönJotpaKoulutus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus'
