import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EBOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/EBOpiskeluoikeudenTila'
import { EBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/EBOpiskeluoikeus'
import { EBOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/EBOpiskeluoikeusjakso'
import { EBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/EBTutkinnonSuoritus'
import { EBTutkinto } from '../../types/fi/oph/koski/schema/EBTutkinto'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { toOppilaitos, toToimipiste } from './utils'

export const createEBOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: EBOpiskeluoikeusjakso['tila'],
  curriculum?: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum'>
) => {
  if (!curriculum) return undefined

  return EBOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: EBOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        EBOpiskeluoikeusjakso({
          alku,
          tila
        })
      ]
    }),
    suoritukset: [
      EBTutkinnonSuoritus({
        koulutusmoduuli: EBTutkinto({
          curriculum
        }),
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })
}
