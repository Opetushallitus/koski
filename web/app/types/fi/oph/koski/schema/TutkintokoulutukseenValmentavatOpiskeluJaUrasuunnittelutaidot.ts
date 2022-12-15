import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Opiskelu- ja urasuunnittelutaidot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot`
 */
export type TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '101'>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '101'>
    laajuus?: LaajuusViikoissa
  } = {}
): TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot',
  tunniste: Koodistokoodiviite({
    koodiarvo: '101',
    koodistoUri: 'koulutuksenosattuva'
  }),
  ...o
})

export const isTutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = (
  a: any
): a is TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot =>
  a?.$class === 'TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot'
