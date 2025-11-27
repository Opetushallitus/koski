import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenAlkuvaihe } from './AikuistenPerusopetuksenAlkuvaihe'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus } from './AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'
import { OmanÄidinkielenOpinnotLaajuusKursseina } from './OmanAidinkielenOpinnotLaajuusKursseina'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenSuoritus`
 */
export type AikuistenPerusopetuksenAlkuvaiheenSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
}

export const AikuistenPerusopetuksenAlkuvaiheenSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AikuistenPerusopetuksenAlkuvaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
}): AikuistenPerusopetuksenAlkuvaiheenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenoppimaaranalkuvaihe',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'aikuistenperusopetuksenoppimaaranalkuvaihe',
      koodistoUri: 'suorituksentyyppi'
    })
  }),
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenSuoritus',
  ...o
})

AikuistenPerusopetuksenAlkuvaiheenSuoritus.className =
  'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenSuoritus' as const

export const isAikuistenPerusopetuksenAlkuvaiheenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenSuoritus'
