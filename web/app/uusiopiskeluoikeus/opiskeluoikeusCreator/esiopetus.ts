import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EsiopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/EsiopetuksenOpiskeluoikeus'
import { EsiopetuksenSuoritus } from '../../types/fi/oph/koski/schema/EsiopetuksenSuoritus'
import { Esiopetus } from '../../types/fi/oph/koski/schema/Esiopetus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { Hankintakoulutus } from '../state/state'
import { toOppilaitos, toToimipiste } from './utils'

export const VARHAISKASVATUKSEN_TOIMIPAIKKA = 'VARHAISKASVATUKSEN_TOIMIPAIKKA'

// Esiopetus
export const createEsiopetuksenOpiskeluoikeus = (
  peruste: Peruste | undefined,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  järjestämismuoto?: Koodistokoodiviite<'vardajarjestamismuoto', any>,
  hankintakoulutus?: Hankintakoulutus
) => {
  if (!peruste || !suorituskieli) return undefined
  if (hankintakoulutus && !järjestämismuoto) return undefined

  return EsiopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: NuortenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        NuortenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
      ]
    }),
    järjestämismuoto,
    suoritukset: [
      EsiopetuksenSuoritus({
        suorituskieli,
        koulutusmoduuli: Esiopetus({
          perusteenDiaarinumero: peruste.koodiarvo,
          tunniste: Koodistokoodiviite({
            koodiarvo: organisaatio.organisaatiotyypit?.includes(
              VARHAISKASVATUKSEN_TOIMIPAIKKA
            )
              ? '001102'
              : '001101',
            koodistoUri: 'koulutus'
          })
        }),
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })
}
