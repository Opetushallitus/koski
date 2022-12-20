import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OmanÄidinkielenOpinnotLaajuusKursseina } from './OmanAidinkielenOpinnotLaajuusKursseina'
import { AikuistenPerusopetus } from './AikuistenPerusopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AikuistenPerusopetuksenOppiaineenSuoritus } from './AikuistenPerusopetuksenOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

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
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const AikuistenPerusopetuksenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AikuistenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
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

export const isAikuistenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppimääränSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenOppimääränSuoritus'
