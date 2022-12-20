import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Vapaan sivistystyön maahanmuuttajien kotoutumiskoulutuksen (OPS 2022) tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutus2022`
 */
export type VSTKotoutumiskoulutus2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutus2022'
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export const VSTKotoutumiskoulutus2022 = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999910'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): VSTKotoutumiskoulutus2022 => ({
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutus2022',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999910',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isVSTKotoutumiskoulutus2022 = (
  a: any
): a is VSTKotoutumiskoulutus2022 =>
  a?.$class === 'fi.oph.koski.schema.VSTKotoutumiskoulutus2022'
