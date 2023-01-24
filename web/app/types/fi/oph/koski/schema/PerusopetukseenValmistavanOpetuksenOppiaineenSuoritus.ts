import { SanallinenPerusopetuksenOppiaineenArviointi } from './SanallinenPerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetukseenValmistavanOpetuksenOppiaine } from './PerusopetukseenValmistavanOpetuksenOppiaine'

/**
 * Perusopetukseen valmistavan opetuksen oppiaineen suoritustiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus`
 */
export type PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus'
  arviointi?: Array<SanallinenPerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavanopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine
}

export const PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<SanallinenPerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavanopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine
}): PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetukseenvalmistavanopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus',
  ...o
})

PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus' as const

export const isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus'
