import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
import { TutkintokoulutukseenValmentavanKoulutus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { toOppilaitos, toToimipiste } from './utils'

// Tutkintokoulutukseen valmentava koulutus
export const createTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeusjakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  tuvaJärjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa'>
) =>
  TutkintokoulutukseenValmentavanOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        TutkintokoulutukseenValmentavanOpiskeluoikeusjakso({
          alku,
          tila,
          opintojenRahoitus
        })
      ]
    }),
    järjestämislupa: tuvaJärjestämislupa,
    suoritukset: [
      TutkintokoulutukseenValmentavanKoulutuksenSuoritus({
        koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })
