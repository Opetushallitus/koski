import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * MusiikinOpintotaso
 *
 * @see `fi.oph.koski.schema.MusiikinOpintotaso`
 */
export type MusiikinOpintotaso = {
  $class: 'fi.oph.koski.schema.MusiikinOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'musiikki'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const MusiikinOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'musiikki'>
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): MusiikinOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'musiikki',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.MusiikinOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isMusiikinOpintotaso = (a: any): a is MusiikinOpintotaso =>
  a?.$class === 'fi.oph.koski.schema.MusiikinOpintotaso'
