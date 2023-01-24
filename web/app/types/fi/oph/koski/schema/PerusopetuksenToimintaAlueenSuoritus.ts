import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenToiminta_Alue } from './PerusopetuksenToimintaAlue'

/**
 * Perusopetuksen toiminta-alueen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta. Suoritukset voidaan kirjata oppiaineiden sijaan toiminta-alueittain, jos opiskelijalle on tehty erityisen tuen päätös
 *
 * @see `fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus`
 */
export type PerusopetuksenToiminta_AlueenSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksentoimintaalue'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
}

export const PerusopetuksenToiminta_AlueenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksentoimintaalue'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
}): PerusopetuksenToiminta_AlueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksentoimintaalue',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus',
  ...o
})

PerusopetuksenToiminta_AlueenSuoritus.className =
  'fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus' as const

export const isPerusopetuksenToiminta_AlueenSuoritus = (
  a: any
): a is PerusopetuksenToiminta_AlueenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus'
