import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 * Oppiaineena vieras tai toinen kotimainen kieli
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenVierasKieli`
 */
export type AikuistenPerusopetuksenAlkuvaiheenVierasKieli = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenVierasKieli'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'A1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export const AikuistenPerusopetuksenAlkuvaiheenVierasKieli = (o: {
  tunniste?: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'A1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}): AikuistenPerusopetuksenAlkuvaiheenVierasKieli => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenVierasKieli',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'A1',
    koodistoUri: 'aikuistenperusopetuksenalkuvaiheenoppiaineet'
  }),
  ...o
})

export const isAikuistenPerusopetuksenAlkuvaiheenVierasKieli = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenVierasKieli =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenVierasKieli'
