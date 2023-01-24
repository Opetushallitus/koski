import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenKurssi } from './AikuistenPerusopetuksenKurssi'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * AikuistenPerusopetuksenKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenKurssinSuoritus`
 */
export type AikuistenPerusopetuksenKurssinSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenKurssinSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}

export const AikuistenPerusopetuksenKurssinSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}): AikuistenPerusopetuksenKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenKurssinSuoritus',
  ...o
})

AikuistenPerusopetuksenKurssinSuoritus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenKurssinSuoritus' as const

export const isAikuistenPerusopetuksenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenKurssinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenKurssinSuoritus'
