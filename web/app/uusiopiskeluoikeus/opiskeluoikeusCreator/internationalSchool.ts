import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  DiplomaVuosiluokanSuoritus,
  isDiplomaVuosiluokanSuoritus
} from '../../types/fi/oph/koski/schema/DiplomaVuosiluokanSuoritus'
import { IBDiplomaLuokkaAste } from '../../types/fi/oph/koski/schema/IBDiplomaLuokkaAste'
import { InternationalSchoolOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeudenLisatiedot'
import { InternationalSchoolOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeudenTila'
import { InternationalSchoolOpiskeluoikeus } from '../../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeus'
import { InternationalSchoolOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/InternationalSchoolOpiskeluoikeusjakso'
import { InternationalSchoolVuosiluokanSuoritus } from '../../types/fi/oph/koski/schema/InternationalSchoolVuosiluokanSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MYPLuokkaAste } from '../../types/fi/oph/koski/schema/MYPLuokkaAste'
import { MYPVuosiluokanSuoritus } from '../../types/fi/oph/koski/schema/MYPVuosiluokanSuoritus'
import { PYPLuokkaAste } from '../../types/fi/oph/koski/schema/PYPLuokkaAste'
import { PYPVuosiluokanSuoritus } from '../../types/fi/oph/koski/schema/PYPVuosiluokanSuoritus'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createInternationalSchoolOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: InternationalSchoolOpiskeluoikeusjakso['tila'],
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', any>,
  grade?: Koodistokoodiviite<'internationalschoolluokkaaste'>,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  maksuton?: boolean | null
) => {
  if (!opintojenRahoitus || !grade || !suorituskieli) return undefined

  const suoritus = createInternationalSchoolVuosiluokanSuoritus(
    grade,
    organisaatio,
    suorituskieli,
    alku
  )

  if (isDiplomaVuosiluokanSuoritus(suoritus) && maksuton === undefined) {
    return undefined
  }

  return (
    suoritus &&
    InternationalSchoolOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: InternationalSchoolOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          InternationalSchoolOpiskeluoikeusjakso({
            alku,
            tila,
            opintojenRahoitus
          })
        ]
      }),
      suoritukset: [suoritus],
      lisätiedot:
        maksuton === undefined
          ? undefined
          : maksuttomuuslisätiedot(
              alku,
              maksuton,
              InternationalSchoolOpiskeluoikeudenLisätiedot
            )
    })
  )
}

const createInternationalSchoolVuosiluokanSuoritus = (
  grade: Koodistokoodiviite<'internationalschoolluokkaaste', any>,
  organisaatio: OrganisaatioHierarkia,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  alku: string
): InternationalSchoolVuosiluokanSuoritus | undefined => {
  switch (grade.koodiarvo) {
    case 'explorer':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
      return PYPVuosiluokanSuoritus({
        koulutusmoduuli: PYPLuokkaAste({ tunniste: grade }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio),
        alkamispäivä: alku
      })
    case '6':
    case '7':
    case '8':
    case '9':
    case '10':
      return MYPVuosiluokanSuoritus({
        koulutusmoduuli: MYPLuokkaAste({ tunniste: grade }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio),
        alkamispäivä: alku
      })
    case '11':
    case '12':
      return DiplomaVuosiluokanSuoritus({
        koulutusmoduuli: IBDiplomaLuokkaAste({ tunniste: grade }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio),
        alkamispäivä: alku
      })
    default:
      return undefined
  }
}
