import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOppiaine } from './NuortenPerusopetuksenOppiaine'

/**
 * Perusopetuksen oppiaineen suoritus osana perusopetukseen valmistavaa opetusta
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa`
 */
export type NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
}

export const NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =
  (o: {
    arviointi?: Array<PerusopetuksenOppiaineenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    suoritustapa?: Koodistokoodiviite<
      'perusopetuksensuoritustapa',
      'erityinentutkinto'
    >
    koulutusmoduuli: NuortenPerusopetuksenOppiaine
  }): NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo:
        'perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa',
    ...o
  })

export const isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =
  (
    a: any
  ): a is NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =>
    a?.$class ===
    'NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa'
