import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * MediataiteenOpintotaso
 *
 * @see `fi.oph.koski.schema.MediataiteenOpintotaso`
 */
export type MediataiteenOpintotaso = {
  $class: 'fi.oph.koski.schema.MediataiteenOpintotaso'
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala', 'mediataiteet'>
  laajuus?: LaajuusOpintopisteissä
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  tunniste: Koodistokoodiviite<'koulutus', '999907'>
}

export const MediataiteenOpintotaso = (
  o: {
    taiteenala?: Koodistokoodiviite<
      'taiteenperusopetustaiteenala',
      'mediataiteet'
    >
    laajuus?: LaajuusOpintopisteissä
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
    tunniste?: Koodistokoodiviite<'koulutus', '999907'>
  } = {}
): MediataiteenOpintotaso => ({
  taiteenala: Koodistokoodiviite({
    koodiarvo: 'mediataiteet',
    koodistoUri: 'taiteenperusopetustaiteenala'
  }),
  $class: 'fi.oph.koski.schema.MediataiteenOpintotaso',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999907',
    koodistoUri: 'koulutus'
  }),
  ...o
})

MediataiteenOpintotaso.className =
  'fi.oph.koski.schema.MediataiteenOpintotaso' as const

export const isMediataiteenOpintotaso = (a: any): a is MediataiteenOpintotaso =>
  a?.$class === 'fi.oph.koski.schema.MediataiteenOpintotaso'
