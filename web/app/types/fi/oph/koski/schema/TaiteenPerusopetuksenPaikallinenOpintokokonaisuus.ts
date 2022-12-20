import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Taiteen perusopetuksen paikallinen opintokokonaisuus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenPaikallinenOpintokokonaisuus`
 */
export type TaiteenPerusopetuksenPaikallinenOpintokokonaisuus = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export const TaiteenPerusopetuksenPaikallinenOpintokokonaisuus = (o: {
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}): TaiteenPerusopetuksenPaikallinenOpintokokonaisuus => ({
  $class:
    'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallinenOpintokokonaisuus',
  ...o
})

export const isTaiteenPerusopetuksenPaikallinenOpintokokonaisuus = (
  a: any
): a is TaiteenPerusopetuksenPaikallinenOpintokokonaisuus =>
  a?.$class ===
  'fi.oph.koski.schema.TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
