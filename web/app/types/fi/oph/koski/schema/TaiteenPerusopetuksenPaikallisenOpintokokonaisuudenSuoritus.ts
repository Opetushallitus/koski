import { TaiteenPerusopetuksenArviointi } from './TaiteenPerusopetuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TaiteenPerusopetuksenPaikallinenOpintokokonaisuus } from './TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import { TaiteenPerusopetuksenOsasuorituksenTunnustus } from './TaiteenPerusopetuksenOsasuorituksenTunnustus'

/**
 * Taiteen perusopetuksen paikallisen opintokokonaisuuden suoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus`
 */
export type TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenpaikallinenopintokokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus
  tunnustettu?: TaiteenPerusopetuksenOsasuorituksenTunnustus
}

export const TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = (o: {
  arviointi?: Array<TaiteenPerusopetuksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'taiteenperusopetuksenpaikallinenopintokokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: TaiteenPerusopetuksenPaikallinenOpintokokonaisuus
  tunnustettu?: TaiteenPerusopetuksenOsasuorituksenTunnustus
}): TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'taiteenperusopetuksenpaikallinenopintokokonaisuus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus',
  ...o
})

TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus' as const

export const isTaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus = (
  a: any
): a is TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallisenOpintokokonaisuudenSuoritus'
