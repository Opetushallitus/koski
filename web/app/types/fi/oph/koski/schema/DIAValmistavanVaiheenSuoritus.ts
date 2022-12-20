import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAValmistavaVaihe } from './DIAValmistavaVaihe'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { DIAOppiaineenValmistavanVaiheenSuoritus } from './DIAOppiaineenValmistavanVaiheenSuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 *
 * @see `fi.oph.koski.schema.DIAValmistavanVaiheenSuoritus`
 */
export type DIAValmistavanVaiheenSuoritus = {
  $class: 'fi.oph.koski.schema.DIAValmistavanVaiheenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: DIAValmistavaVaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const DIAValmistavanVaiheenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: DIAValmistavaVaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): DIAValmistavanVaiheenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diavalmistavavaihe',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: DIAValmistavaVaihe({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'diavalmistavavaihe',
      koodistoUri: 'suorituksentyyppi'
    })
  }),
  $class: 'fi.oph.koski.schema.DIAValmistavanVaiheenSuoritus',
  ...o
})

export const isDIAValmistavanVaiheenSuoritus = (
  a: any
): a is DIAValmistavanVaiheenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DIAValmistavanVaiheenSuoritus'
