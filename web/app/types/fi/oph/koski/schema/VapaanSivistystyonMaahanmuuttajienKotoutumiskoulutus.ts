import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus`
 */
export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export const VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999910'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999910',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus = (
  a: any
): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =>
  a?.$class === 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
