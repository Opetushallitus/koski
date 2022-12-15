import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OmanÄidinkielenOpinnotLaajuusKursseina } from './OmanAidinkielenOpinnotLaajuusKursseina'
import { AikuistenPerusopetuksenAlkuvaihe } from './AikuistenPerusopetuksenAlkuvaihe'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus } from './AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

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
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const AikuistenPerusopetuksenAlkuvaiheenSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: AikuistenPerusopetuksenAlkuvaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
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

export const isAikuistenPerusopetuksenAlkuvaiheenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenSuoritus'
