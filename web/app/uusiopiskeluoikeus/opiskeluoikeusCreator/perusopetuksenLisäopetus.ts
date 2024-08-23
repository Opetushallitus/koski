import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'
import { PerusopetuksenLisäopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenOpiskeluoikeus'
import { PerusopetuksenLisäopetuksenSuoritus } from '../../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenSuoritus'
import { PerusopetuksenLisäopetus } from '../../types/fi/oph/koski/schema/PerusopetuksenLisaopetus'
import { toOppilaitos, toToimipiste, maksuttomuuslisätiedot } from './utils'

// Perusopetuksen lisäopetus
export const createPerusopetuksenLisäopetuksenOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton: boolean | null
) =>
  PerusopetuksenLisäopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: NuortenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        NuortenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
      ]
    }),
    suoritukset: [
      PerusopetuksenLisäopetuksenSuoritus({
        koulutusmoduuli: PerusopetuksenLisäopetus({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    ],
    lisätiedot: maksuttomuuslisätiedot(
      alku,
      maksuton,
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
    )
  })
