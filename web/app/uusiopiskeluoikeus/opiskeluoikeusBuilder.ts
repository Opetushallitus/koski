import { Peruste } from '../appstate/peruste'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { AikuistenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeus'
import { AikuistenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { AikuistenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { AikuistenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppimaaranSuoritus'
import { AikuistenPerusopetus } from '../types/fi/oph/koski/schema/AikuistenPerusopetus'
import { EiTiedossaOppiaine } from '../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Maksuttomuus } from '../types/fi/oph/koski/schema/Maksuttomuus'
import { MaksuttomuusTieto } from '../types/fi/oph/koski/schema/MaksuttomuusTieto'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { NuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { NuortenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { NuortenPerusopetus } from '../types/fi/oph/koski/schema/NuortenPerusopetus'
import { OidOrganisaatio } from '../types/fi/oph/koski/schema/OidOrganisaatio'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { PerusopetukseenValmistavaOpetus } from '../types/fi/oph/koski/schema/PerusopetukseenValmistavaOpetus'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso } from '../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
import { PerusopetukseenValmistavanOpetuksenSuoritus } from '../types/fi/oph/koski/schema/PerusopetukseenValmistavanOpetuksenSuoritus'
import { PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'
import { PerusopetuksenLisäopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenOpiskeluoikeus'
import { PerusopetuksenLisäopetuksenSuoritus } from '../types/fi/oph/koski/schema/PerusopetuksenLisaopetuksenSuoritus'
import { PerusopetuksenLisäopetus } from '../types/fi/oph/koski/schema/PerusopetuksenLisaopetus'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'

export const createOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  opiskeluoikeudenTyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi'>,
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste | undefined,
  alku: string,
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton?: boolean | null,
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus'>
): Opiskeluoikeus | undefined => {
  switch (opiskeluoikeudenTyyppi.koodiarvo) {
    case 'perusopetus':
      if (!peruste) return undefined
      return createPerusopetuksenOpiskeluoikeus(
        suorituksenTyyppi,
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )
    case 'perusopetukseenvalmistavaopetus':
      if (!peruste) return undefined
      return createPerusopetukseenValmistavaOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )
    case 'perusopetuksenlisaopetus':
      if (!peruste || maksuton === undefined) return undefined
      return createPerusopetuksenLisäopetuksenOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli,
        maksuton
      )

    case 'aikuistenperusopetus':
      if (!peruste || maksuton === undefined || !opintojenRahoitus) {
        return undefined
      }
      return createAikuistenPerusopetuksenOpiskeluoikeus(
        suorituksenTyyppi,
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli,
        maksuton,
        opintojenRahoitus
      )

    case 'esiopetus':
      if (!peruste) return undefined
      return createEsiopetuksenOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )

    default:
      console.error(
        'createOpiskeluoikeus does not support',
        opiskeluoikeudenTyyppi.koodiarvo
      )
  }
}

const isKoodiarvo = <A extends string, U extends string>(
  koodi: Koodistokoodiviite<U>,
  koodiarvo: A
): koodi is Koodistokoodiviite<U, A> => koodi.koodiarvo === koodiarvo

const toToimipiste = (org: OrganisaatioHierarkia): Toimipiste =>
  Toimipiste({
    oid: org.oid,
    nimi: org.nimi,
    kotipaikka: org.kotipaikka as Koodistokoodiviite<'kunta'>
  })

const toOppilaitos = (org: OrganisaatioHierarkia): Oppilaitos =>
  Oppilaitos({
    oid: org.oid
  })

const maksuttomuuslisätiedot = <T extends MaksuttomuusTieto>(
  alku: string,
  maksuton: boolean | null,
  lisätietoCtor: (p: { maksuttomuus: Maksuttomuus[] }) => T
): T | undefined =>
  maksuton === null
    ? undefined
    : lisätietoCtor({
        maksuttomuus: [Maksuttomuus({ alku, maksuton })]
      })

// Perusopetus

const createPerusopetuksenOpiskeluoikeus = (
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

// Perusopetukseen valmistava opetus

const createPerusopetukseenValmistavaOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>
) =>
  PerusopetukseenValmistavanOpetuksenOpiskeluoikeus({
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

// Perusopetuksen lisäopetus

const createPerusopetuksenLisäopetuksenOpiskeluoikeus = (
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

// Aikuisten perusopetus

const createAikuistenPerusopetuksenOpiskeluoikeus = (
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
