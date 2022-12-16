import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuuPerusopetuksenLisäopetuksenKoulutusmoduuli } from './MuuPerusopetuksenLisaopetuksenKoulutusmoduuli'

/**
 * Muu perusopetuksen lisäopetuksessa suoritettu opintokokonaisuus
 *
 * @see `fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenSuoritus`
 */
export type MuuPerusopetuksenLisäopetuksenSuoritus = {
  $class: 'fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muuperusopetuksenlisaopetuksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuPerusopetuksenLisäopetuksenKoulutusmoduuli
}

export const MuuPerusopetuksenLisäopetuksenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'muuperusopetuksenlisaopetuksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuPerusopetuksenLisäopetuksenKoulutusmoduuli
}): MuuPerusopetuksenLisäopetuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muuperusopetuksenlisaopetuksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuuPerusopetuksenLisäopetuksenSuoritus',
  ...o
})

export const isMuuPerusopetuksenLisäopetuksenSuoritus = (
  a: any
): a is MuuPerusopetuksenLisäopetuksenSuoritus =>
  a?.$class === 'MuuPerusopetuksenLisäopetuksenSuoritus'
