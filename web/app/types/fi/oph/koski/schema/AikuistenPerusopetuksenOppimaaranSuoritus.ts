import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetus } from './AikuistenPerusopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AikuistenPerusopetuksenOppiaineenSuoritus } from './AikuistenPerusopetuksenOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { OmanÄidinkielenOpinnotLaajuusKursseina } from './OmanAidinkielenOpinnotLaajuusKursseina'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus`
 */
export type AikuistenPerusopetuksenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaara'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
}

export const AikuistenPerusopetuksenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaara'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AikuistenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
}): AikuistenPerusopetuksenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: AikuistenPerusopetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '201101',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus',
  ...o
})

AikuistenPerusopetuksenOppimääränSuoritus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus' as const

export const isAikuistenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppimääränSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus'
