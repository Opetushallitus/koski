import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EiTiedossaOppiaine } from '../../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { NuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { NuortenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { NuortenPerusopetus } from '../../types/fi/oph/koski/schema/NuortenPerusopetus'
import { PerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { toOppilaitos, isKoodiarvo, toToimipiste } from './utils'

// Perusopetus
export const createPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>
) =>
  PerusopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: NuortenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        NuortenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
      ]
    }),
    suoritukset: [
      isKoodiarvo(suorituksenTyyppi, 'perusopetuksenoppimaara')
        ? NuortenPerusopetuksenOppimääränSuoritus({
            koulutusmoduuli: NuortenPerusopetus({
              perusteenDiaarinumero: peruste.koodiarvo
            }),
            suorituskieli,
            suoritustapa: Koodistokoodiviite({
              koodiarvo: 'koulutus',
              koodistoUri: 'perusopetuksensuoritustapa'
            }),
            toimipiste: toToimipiste(organisaatio)
          })
        : NuortenPerusopetuksenOppiaineenOppimääränSuoritus({
            koulutusmoduuli: EiTiedossaOppiaine({
              perusteenDiaarinumero: peruste.koodiarvo
            }),
            suorituskieli,
            suoritustapa: Koodistokoodiviite({
              koodiarvo: 'koulutus',
              koodistoUri: 'perusopetuksensuoritustapa'
            }),
            toimipiste: toToimipiste(organisaatio)
          })
    ]
  })
