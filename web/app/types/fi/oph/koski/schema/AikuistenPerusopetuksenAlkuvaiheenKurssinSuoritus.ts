import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenAlkuvaiheenKurssi } from './AikuistenPerusopetuksenAlkuvaiheenKurssi'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus`
 */
export type AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}

export const AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}): AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenalkuvaiheenkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus',
  ...o
})

AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus' as const

export const isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'
