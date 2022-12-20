import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Ammatillisen koulutuksen opinnot ja niihin valmentautuminen
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot`
 */
export type TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '105'>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '105'>
    laajuus?: LaajuusViikoissa
  } = {}
): TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot',
  tunniste: Koodistokoodiviite({
    koodiarvo: '105',
    koodistoUri: 'koulutuksenosattuva'
  }),
  ...o
})

export const isTutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = (
  a: any
): a is TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot'
