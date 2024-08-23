import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Maksuttomuus } from '../../types/fi/oph/koski/schema/Maksuttomuus'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
import { TutkintokoulutukseenValmentavanKoulutus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { toOppilaitos, toToimipiste } from './utils'

// Tutkintokoulutukseen valmentava koulutus
export const createTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  peruste: Peruste | undefined,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeusjakso['tila'],
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  tuvaJärjestämislupa?: Koodistokoodiviite<'tuvajarjestamislupa'>,
  maksuton?: boolean | null
) => {
  if (
    !peruste ||
    !tuvaJärjestämislupa ||
    !opintojenRahoitus ||
    !suorituskieli ||
    maksuton === undefined
  ) {
    return undefined
  }

  return TutkintokoulutukseenValmentavanOpiskeluoikeus({
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
}

const createTutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot = (
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa'>,
  alku: string,
  maksuton: boolean | null
) => {
  const lisätiedot =
    maksuton === null
      ? {}
      : { maksuttomuus: [Maksuttomuus({ alku, maksuton })] }

  switch (järjestämislupa.koodiarvo) {
    case 'ammatillinen':
      return TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
        lisätiedot
      )
    case 'lukio':
      return TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
        lisätiedot
      )
    case 'perusopetus':
      return TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        lisätiedot
      )
    default:
      return undefined
  }
}
