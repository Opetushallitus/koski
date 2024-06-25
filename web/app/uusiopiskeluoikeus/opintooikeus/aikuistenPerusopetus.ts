import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { AikuistenPerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeus'
import { AikuistenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { AikuistenPerusopetuksenOppiaineenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { AikuistenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppimaaranSuoritus'
import { AikuistenPerusopetus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetus'
import { EiTiedossaOppiaine } from '../../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import {
  toOppilaitos,
  isKoodiarvo,
  toToimipiste,
  maksuttomuuslisätiedot
} from './utils'

// Aikuisten perusopetus
export const createAikuistenPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton: boolean | null,
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>
) =>
  AikuistenPerusopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: AikuistenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        AikuistenPerusopetuksenOpiskeluoikeusjakso({
          alku,
          tila,
          opintojenRahoitus
        })
      ]
    }),
    suoritukset: [
      isKoodiarvo(suorituksenTyyppi, 'aikuistenperusopetuksenoppimaara')
        ? AikuistenPerusopetuksenOppimääränSuoritus({
            koulutusmoduuli: AikuistenPerusopetus({
              perusteenDiaarinumero: peruste.koodiarvo
            }),
            suorituskieli,
            suoritustapa: Koodistokoodiviite({
              koodiarvo: 'koulutus',
              koodistoUri: 'perusopetuksensuoritustapa'
            }),
            toimipiste: toToimipiste(organisaatio)
          })
        : AikuistenPerusopetuksenOppiaineenOppimääränSuoritus({
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
    ],
    lisätiedot: maksuttomuuslisätiedot(
      alku,
      maksuton,
      AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
    )
  })
