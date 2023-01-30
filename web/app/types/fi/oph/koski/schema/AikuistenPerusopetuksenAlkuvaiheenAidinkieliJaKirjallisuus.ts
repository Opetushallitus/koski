import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus`
 */
export type AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'AI'
  >
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
}

export const AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = (o: {
  tunniste?: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'AI'
  >
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
}): AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus => ({
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'aikuistenperusopetuksenalkuvaiheenoppiaineet'
  }),
  ...o
})

AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus' as const

export const isAikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus'
