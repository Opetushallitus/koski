import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { DIAOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeudenLisatiedot'
import { DIAOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeudenTila'
import { DIAOpiskeluoikeus } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeus'
import { DIAOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeusjakso'
import { DIATutkinnonSuoritus } from '../../types/fi/oph/koski/schema/DIATutkinnonSuoritus'
import { DIAValmistavanVaiheenSuoritus } from '../../types/fi/oph/koski/schema/DIAValmistavanVaiheenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createDIAOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: DIAOpiskeluoikeusjakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton: boolean | null
) => {
  const suoritus = createDIAPäätasonSuoritus(
    suorituksenTyyppi,
    suorituskieli,
    organisaatio
  )

  return (
    suoritus &&
    DIAOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: DIAOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          DIAOpiskeluoikeusjakso({ alku, tila, opintojenRahoitus })
        ]
      }),
      suoritukset: [suoritus],
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        DIAOpiskeluoikeudenLisätiedot
      )
    })
  )
}

const createDIAPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  organisaatio: OrganisaatioHierarkia
) => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'diatutkintovaihe':
      return DIATutkinnonSuoritus({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'diavalmistavavaihe':
      return DIAValmistavanVaiheenSuoritus({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    default:
      return undefined
  }
}
