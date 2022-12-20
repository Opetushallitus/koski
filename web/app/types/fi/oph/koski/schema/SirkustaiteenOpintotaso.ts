import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * SirkustaiteenOpintotaso
 *
 * @see `fi.oph.koski.schema.SirkustaiteenOpintotaso`
 */
export type SirkustaiteenOpintotaso = {
  $class: 'fi.oph.koski.schema.SirkustaiteenOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'sirkustaide'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const SirkustaiteenOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<
      'taiteenperusopetustaiteenala',
      'sirkustaide'
    >
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): SirkustaiteenOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'sirkustaide',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.SirkustaiteenOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isSirkustaiteenOpintotaso = (
  a: any
): a is SirkustaiteenOpintotaso =>
  a?.$class === 'fi.oph.koski.schema.SirkustaiteenOpintotaso'
