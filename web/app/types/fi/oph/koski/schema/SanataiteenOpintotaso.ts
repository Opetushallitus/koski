import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * SanataiteenOpintotaso
 *
 * @see `fi.oph.koski.schema.SanataiteenOpintotaso`
 */
export type SanataiteenOpintotaso = {
  $class: 'fi.oph.koski.schema.SanataiteenOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'sanataide'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const SanataiteenOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'sanataide'>
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): SanataiteenOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'sanataide',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.SanataiteenOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isSanataiteenOpintotaso = (a: any): a is SanataiteenOpintotaso =>
  a?.$class === 'SanataiteenOpintotaso'
