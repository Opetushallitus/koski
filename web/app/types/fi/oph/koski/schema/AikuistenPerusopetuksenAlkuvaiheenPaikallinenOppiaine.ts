import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine`
 */
export type AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export const AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}): AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine => ({
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine',
  ...o
})

AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine' as const

export const isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'
