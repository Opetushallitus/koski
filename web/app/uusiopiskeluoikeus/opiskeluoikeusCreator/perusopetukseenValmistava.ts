import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { PerusopetukseenValmistavaOpetus } from '../../types/fi/oph/koski/schema/PerusopetukseenValmistavaOpetus'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso } from '../../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
import { PerusopetukseenValmistavanOpetuksenSuoritus } from '../../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenSuoritus'
import { toOppilaitos, toToimipiste } from './utils'

// Perusopetukseen valmistava opetus
export const createPerusopetukseenValmistavaOpiskeluoikeus = (
  peruste: Peruste | undefined,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'> | undefined
) => {
  if (!peruste || !suorituskieli) return undefined

  return PerusopetukseenValmistavanOpetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso({ alku, tila })
      ]
    }),
    suoritukset: [
      PerusopetukseenValmistavanOpetuksenSuoritus({
        koulutusmoduuli: PerusopetukseenValmistavaOpetus({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })
}
