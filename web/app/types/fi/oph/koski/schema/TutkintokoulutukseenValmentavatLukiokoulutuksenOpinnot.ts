import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Lukiokoulutuksen opinnot ja niihin valmentautuminen
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot`
 */
export type TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '106'>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '106'>
    laajuus?: LaajuusViikoissa
  } = {}
): TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot',
  tunniste: Koodistokoodiviite({
    koodiarvo: '106',
    koodistoUri: 'koulutuksenosattuva'
  }),
  ...o
})

export const isTutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = (
  a: any
): a is TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot =>
  a?.$class === 'TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot'
