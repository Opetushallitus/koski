import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOppiaine } from './NuortenPerusopetuksenOppiaine'

/**
 * Perusopetuksen oppiaineen suoritus osana perusopetuksen lisäopetusta
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenOppiaineenSuoritus`
 */
export type PerusopetuksenLisäopetuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOppiaineenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  korotus: boolean
  yksilöllistettyOppimäärä: boolean
}

export const PerusopetuksenLisäopetuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  korotus: boolean
  yksilöllistettyOppimäärä?: boolean
}): PerusopetuksenLisäopetuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenlisaopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOppiaineenSuoritus',
  yksilöllistettyOppimäärä: false,
  ...o
})

export const isPerusopetuksenLisäopetuksenOppiaineenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenOppiaineenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOppiaineenSuoritus'
