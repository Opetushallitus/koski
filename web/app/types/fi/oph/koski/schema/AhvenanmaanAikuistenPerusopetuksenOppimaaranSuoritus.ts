import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AhvenanmaanPerusopetus } from './AhvenanmaanPerusopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from './AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Ahvenanmaan perusopetuksen oppimäärän suoritus muille kuin oppivelvollisille. Nämä suoritukset näkyvät päättötodistuksella.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus`
 */
export type AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppimaaraaikuiset'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AhvenanmaanPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppimaaraaikuiset'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AhvenanmaanPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksenoppimaaraaikuiset',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: AhvenanmaanPerusopetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '201101',
      koodistoUri: 'koulutus'
    })
  }),
  $class:
    'fi.oph.koski.schema.AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus',
  ...o
})

AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus' as const

export const isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus'
