import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * KuvataiteenOpintotaso
 *
 * @see `fi.oph.koski.schema.KuvataiteenOpintotaso`
 */
export type KuvataiteenOpintotaso = {
  $class: 'fi.oph.koski.schema.KuvataiteenOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'kuvataide'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const KuvataiteenOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'kuvataide'>
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): KuvataiteenOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'kuvataide',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.KuvataiteenOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isKuvataiteenOpintotaso = (a: any): a is KuvataiteenOpintotaso =>
  a?.$class === 'KuvataiteenOpintotaso'
