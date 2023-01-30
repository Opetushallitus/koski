import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetuksenVuosiluokanSuorituksenLiite } from './PerusopetuksenVuosiluokanSuorituksenLiite'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from './OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'
import { PerusopetuksenKäyttäytymisenArviointi } from './PerusopetuksenKayttaytymisenArviointi'
import { PerusopetuksenLuokkaAste } from './PerusopetuksenLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OppiaineenTaiToiminta_AlueenSuoritus } from './OppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella
 *
 * @see `fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus`
 */
export type PerusopetuksenVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenvuosiluokka'>
  liitetiedot?: Array<PerusopetuksenVuosiluokanSuorituksenLiite>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  käyttäytymisenArvio?: PerusopetuksenKäyttäytymisenArviointi
  koulutusmoduuli: PerusopetuksenLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PerusopetuksenVuosiluokanSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenvuosiluokka'>
  liitetiedot?: Array<PerusopetuksenVuosiluokanSuorituksenLiite>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  käyttäytymisenArvio?: PerusopetuksenKäyttäytymisenArviointi
  koulutusmoduuli: PerusopetuksenLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PerusopetuksenVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus',
  ...o
})

PerusopetuksenVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus' as const

export const isPerusopetuksenVuosiluokanSuoritus = (
  a: any
): a is PerusopetuksenVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus'
