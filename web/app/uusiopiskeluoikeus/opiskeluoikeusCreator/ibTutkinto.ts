import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { DIAOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeusjakso'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenLisatiedot'
import { LukionOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { LukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { PreIBSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2015'
import { PreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2019'
import { SuoritusClass } from '../../types/fi/oph/koski/typemodel/SuoritusClass'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createIBOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: DIAOpiskeluoikeusjakso['tila'],
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  maksuton?: boolean | null
) => {
  if (!opintojenRahoitus || !suorituskieli || maksuton === undefined) {
    return undefined
  }

  const suoritus = createIBPäätasonSuoritus(
    suorituksenTyyppi,
    suorituskieli,
    organisaatio
  )

  return (
    suoritus &&
    IBOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: LukionOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          LukionOpiskeluoikeusjakso({ alku, tila, opintojenRahoitus })
        ]
      }),
      suoritukset: [suoritus],
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        LukionOpiskeluoikeudenLisätiedot
      )
    })
  )
}

const createIBPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  organisaatio: OrganisaatioHierarkia
) => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'ibtutkinto':
      return IBTutkinnonSuoritus({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'preiboppimaara':
      return PreIBSuoritus2015({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case UIPreIb2019PääsuorituksenTyyppi:
      return PreIBSuoritus2019({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    default:
      return undefined
  }
}

export const UIPreIb2019PääsuorituksenTyyppi = 'preiboppimaara2019'
