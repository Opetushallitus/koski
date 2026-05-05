import { AhvenanmaanPerusopetuksenOppiaineenArviointi } from './AhvenanmaanPerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AhvenanmaanPerusopetuksenToimintaAlue } from './AhvenanmaanPerusopetuksenToimintaAlue'

/**
 * Ahvenanmaan perusopetuksen toiminta-alueen suoritus osana oppimäärän tai vuosiluokan suoritusta.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlueenSuoritus`
 */
export type AhvenanmaanPerusopetuksenToimintaAlueenSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlueenSuoritus'
  arviointi?: Array<AhvenanmaanPerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksentoimintaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AhvenanmaanPerusopetuksenToimintaAlue
}

export const AhvenanmaanPerusopetuksenToimintaAlueenSuoritus = (o: {
  arviointi?: Array<AhvenanmaanPerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksentoimintaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AhvenanmaanPerusopetuksenToimintaAlue
}): AhvenanmaanPerusopetuksenToimintaAlueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksentoimintaalue',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlueenSuoritus',
  ...o
})

AhvenanmaanPerusopetuksenToimintaAlueenSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlueenSuoritus' as const

export const isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenToimintaAlueenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenToimintaAlueenSuoritus'
