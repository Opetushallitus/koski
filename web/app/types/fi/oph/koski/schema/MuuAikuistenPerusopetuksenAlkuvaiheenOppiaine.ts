import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine`
 */
export type MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine = {
  $class: 'fi.oph.koski.schema.MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'MA' | 'YH' | 'YL' | 'TE' | 'OP'
  >
}

export const MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine = (o: {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'MA' | 'YH' | 'YL' | 'TE' | 'OP'
  >
}): MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine => ({
  $class: 'fi.oph.koski.schema.MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine',
  ...o
})

export const isMuuAikuistenPerusopetuksenAlkuvaiheenOppiaine = (
  a: any
): a is MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine =>
  a?.$class ===
  'fi.oph.koski.schema.MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine'
