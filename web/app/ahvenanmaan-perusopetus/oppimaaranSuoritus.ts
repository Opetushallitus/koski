import {
  AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppimaaranSuoritus'

export type AhvenanmaanOppimääränSuoritus =
  | AhvenanmaanPerusopetuksenOppimääränSuoritus
  | AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus

// Oppivelvollisten ja muiden kuin oppivelvollisten oppimäärän suoritukset ovat
// eri luokkia, ja generoidut is-funktiot vertaavat $classia tarkasti. Kumpikin
// on päättötodistus, joten sitä koskevat säännöt tarvitsevat molemmat.
export const isAhvenanmaanOppimääränSuoritus = (
  s: unknown
): s is AhvenanmaanOppimääränSuoritus =>
  isAhvenanmaanPerusopetuksenOppimääränSuoritus(s) ||
  isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus(s)
