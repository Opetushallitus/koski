import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeus } from '../../types/fi/oph/koski/schema/EuropeanSchoolOfHelsinkiOpiskeluoikeus'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NurseryLuokkaAste } from '../../types/fi/oph/koski/schema/NurseryLuokkaAste'
import { NurseryVuosiluokanSuoritus } from '../../types/fi/oph/koski/schema/NurseryVuosiluokanSuoritus'
import { toOppilaitos, toToimipiste } from './utils'

export const createEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum'>
) =>
  EuropeanSchoolOfHelsinkiOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso({
          alku,
          tila,
          opintojenRahoitus
        })
      ]
    }),
    suoritukset: [
      NurseryVuosiluokanSuoritus({
        koulutusmoduuli: NurseryLuokkaAste({
          tunniste: Koodistokoodiviite({
            koodiarvo: 'N1',
            koodistoUri: 'europeanschoolofhelsinkiluokkaaste'
          }),
          curriculum
        }),
        toimipiste: toToimipiste(organisaatio),
        alkamispäivä: alku
      })
    ]
  })
