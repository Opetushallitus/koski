import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenToiminta_Alue } from './PerusopetuksenToimintaAlue'

/**
 * Perusopetuksen toiminta-alueen suoritus osana perusopetuksen lisäopetusta
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus`
 */
export type PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksentoimintaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
  korotus: boolean
}

export const PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksentoimintaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
  korotus?: boolean
}): PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenlisaopetuksentoimintaalue',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus',
  korotus: false,
  ...o
})

PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus.className =
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus' as const

export const isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus'
