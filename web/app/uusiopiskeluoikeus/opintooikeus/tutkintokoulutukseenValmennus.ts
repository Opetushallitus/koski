import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
import { TutkintokoulutukseenValmentavanKoulutus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

// Tutkintokoulutukseen valmentava koulutus
export const createTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeusjakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  tuvaJärjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa'>,
  maksuton: boolean | null
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
    ],
    lisätiedot: createTutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot(
      tuvaJärjestämislupa,
      alku,
      maksuton
    )
  })

const createTutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot = (
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa'>,
  alku: string,
  maksuton: boolean | null
) => {
  switch (järjestämislupa.koodiarvo) {
    case 'ammatillinen':
      return maksuttomuuslisätiedot(
        alku,
        maksuton,
        TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
      )
    case 'lukio':
      return maksuttomuuslisätiedot(
        alku,
        maksuton,
        TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
      )
    case 'perusopetus':
      return maksuttomuuslisätiedot(
        alku,
        maksuton,
        TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot
      )
    default:
      return undefined
  }
}
