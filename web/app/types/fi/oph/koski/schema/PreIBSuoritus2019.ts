import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { SuullisenKielitaidonKoe2019 } from './SuullisenKielitaidonKoe2019'
import { OmanÄidinkielenOpinnotLaajuusOpintopisteinä } from './OmanAidinkielenOpinnotLaajuusOpintopisteina'
import { PuhviKoe2019 } from './PuhviKoe2019'
import { PreIBKoulutusmoduuli2019 } from './PreIBKoulutusmoduuli2019'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PreIBSuorituksenOsasuoritus2019 } from './PreIBSuorituksenOsasuoritus2019'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.PreIBSuoritus2019`
 */
export type PreIBSuoritus2019 = {
  $class: 'fi.oph.koski.schema.PreIBSuoritus2019'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusOpintopisteinä
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: PreIBKoulutusmoduuli2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PreIBSuoritus2019 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusOpintopisteinä
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli?: PreIBKoulutusmoduuli2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PreIBSuoritus2019 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'preiboppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: PreIBKoulutusmoduuli2019({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'preiboppimaara2019',
      koodistoUri: 'suorituksentyyppi'
    })
  }),
  $class: 'fi.oph.koski.schema.PreIBSuoritus2019',
  ...o
})

export const isPreIBSuoritus2019 = (a: any): a is PreIBSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.PreIBSuoritus2019'
