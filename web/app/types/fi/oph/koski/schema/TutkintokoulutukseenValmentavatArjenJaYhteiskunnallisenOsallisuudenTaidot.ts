import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Arjen ja yhteiskunnallisen osallisuuden taidot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot`
 */
export type TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot'
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '103'>
    laajuus?: LaajuusViikoissa
  }

export const TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  (
    o: {
      tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '103'>
      laajuus?: LaajuusViikoissa
    } = {}
  ): TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot',
    tunniste: Koodistokoodiviite({
      koodiarvo: '103',
      koodistoUri: 'koulutuksenosattuva'
    }),
    ...o
  })

export const isTutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =>
    a?.$class ===
    'TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot'
