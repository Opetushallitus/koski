import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * Perustaitojen vahvistaminen
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen`
 */
export type TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '107'>
  laajuus?: LaajuusViikoissa
}

export const TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutuksenosattuva', '107'>
    laajuus?: LaajuusViikoissa
  } = {}
): TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen => ({
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen',
  tunniste: Koodistokoodiviite({
    koodiarvo: '107',
    koodistoUri: 'koulutuksenosattuva'
  }),
  ...o
})

TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen.className =
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen' as const

export const isTutkintokoulutukseenValmentavaPerustaitojenVahvistaminen = (
  a: any
): a is TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen'
