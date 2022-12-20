import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from './OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'
import { NuortenPerusopetus } from './NuortenPerusopetus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OppiaineenTaiToiminta_AlueenSuoritus } from './OppiaineenTaiToimintaAlueenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppimääränSuoritus`
 */
export type NuortenPerusopetuksenOppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppimääränSuoritus'
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: NuortenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const NuortenPerusopetuksenOppimääränSuoritus = (o: {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: NuortenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): NuortenPerusopetuksenOppimääränSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenoppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: NuortenPerusopetus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '201101',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppimääränSuoritus',
  ...o
})

export const isNuortenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppimääränSuoritus =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenOppimääränSuoritus'
