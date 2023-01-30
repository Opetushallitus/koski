import { TaiteenPerusopetuksenPaikallinenOpintokokonaisuus } from './TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import { TaiteenPerusopetuksenArviointi } from './TaiteenPerusopetuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Taiteen perusopetuksen paikallisen opintokokonaisuuden suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus`
 */
export type TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
  koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenpaikallinenopintokokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = (o: {
  koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenpaikallinenopintokokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus => ({
  $class:
    'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'taiteenperusopetuksenpaikallinenopintokokonaisuus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus' as const

export const isTaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = (
  a: any
): a is TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
