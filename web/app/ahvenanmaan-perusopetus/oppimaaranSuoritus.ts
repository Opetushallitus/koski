import { t } from '../i18n/i18n'
import {
  AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanPerusopetuksenOppimääränSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppimaaranSuoritus'
import { AhvenanmaanPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenPaatasonSuoritus'

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

// Oppimäärän koulutuskoodi 201101 on valtakunnallinen, ja sen ruotsinkielinen
// nimi on manner-Suomen termi "Grundläggande utbildning". Ahvenanmaalla
// käytetään omaa termiä, joten nimi tulee lokalisaatioavaimesta.
export const ahvenanmaanKoulutuksenNimi = (
  s: AhvenanmaanPerusopetuksenPäätasonSuoritus | undefined
): string =>
  isAhvenanmaanOppimääränSuoritus(s)
    ? t('ahvenanmaan-perusopetus-koulutuksen-nimi')
    : t(s?.koulutusmoduuli.tunniste.nimi)
