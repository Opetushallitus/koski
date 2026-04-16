import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AhvenanmaanPerusopetus } from './AhvenanmaanPerusopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from './AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from './AhvenanmaanOmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'

/**
 * Ahvenanmaan perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppimääränSuoritus`
 */
export type AhvenanmaanPerusopetuksenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppimääränSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppimaara'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AhvenanmaanPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
}

export const AhvenanmaanPerusopetuksenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppimaara'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AhvenanmaanPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
}): AhvenanmaanPerusopetuksenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: AhvenanmaanPerusopetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '201101',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppimääränSuoritus',
  ...o
})

AhvenanmaanPerusopetuksenOppimääränSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppimääränSuoritus' as const

export const isAhvenanmaanPerusopetuksenOppimääränSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenOppimääränSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppimääränSuoritus'
