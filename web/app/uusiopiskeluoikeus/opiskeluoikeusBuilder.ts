import { Peruste } from '../appstate/peruste'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { AikuistenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeus'
import { AikuistenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { AikuistenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { AikuistenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppimaaranSuoritus'
import { AikuistenPerusopetus } from '../types/fi/oph/koski/schema/AikuistenPerusopetus'
import { ArkkitehtuurinOpintotaso } from '../types/fi/oph/koski/schema/ArkkitehtuurinOpintotaso'
import { EiTiedossaOppiaine } from '../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { EsiopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/EsiopetuksenOpiskeluoikeus'
import { EsiopetuksenSuoritus } from '../types/fi/oph/koski/schema/EsiopetuksenSuoritus'
import { Esiopetus } from '../types/fi/oph/koski/schema/Esiopetus'
import { KäsityönOpintotaso } from '../types/fi/oph/koski/schema/KasityonOpintotaso'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KuvataiteenOpintotaso } from '../types/fi/oph/koski/schema/KuvataiteenOpintotaso'
import { Maksuttomuus } from '../types/fi/oph/koski/schema/Maksuttomuus'
import { MaksuttomuusTieto } from '../types/fi/oph/koski/schema/MaksuttomuusTieto'
import { MediataiteenOpintotaso } from '../types/fi/oph/koski/schema/MediataiteenOpintotaso'
import { MusiikinOpintotaso } from '../types/fi/oph/koski/schema/MusiikinOpintotaso'
import { MuuKuinSäänneltyKoulutus } from '../types/fi/oph/koski/schema/MuuKuinSaanneltyKoulutus'
import { MuunKuinSäännellynKoulutuksenLisätiedot } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenLisatiedot'
import { MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'
import { MuunKuinSäännellynKoulutuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeus'
import { MuunKuinSäännellynKoulutuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { MuunKuinSäännellynKoulutuksenTila } from '../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenTila'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { NuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { NuortenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { NuortenPerusopetus } from '../types/fi/oph/koski/schema/NuortenPerusopetus'
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
import { SanataiteenOpintotaso } from '../types/fi/oph/koski/schema/SanataiteenOpintotaso'
import { SirkustaiteenOpintotaso } from '../types/fi/oph/koski/schema/SirkustaiteenOpintotaso'
import { TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranPerusopintojenSuoritus'
import { TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranSyventavienOpintojenSuoritus'
import { TaiteenPerusopetuksenOpintotaso } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpintotaso'
import { TaiteenPerusopetuksenOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeudenTila'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeusjakso'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TanssinOpintotaso } from '../types/fi/oph/koski/schema/TanssinOpintotaso'
import { TeatteritaiteenOpintotaso } from '../types/fi/oph/koski/schema/TeatteritaiteenOpintotaso'
import { Toimipiste } from '../types/fi/oph/koski/schema/Toimipiste'
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
import { TutkintokoulutukseenValmentavanKoulutus } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanKoulutus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import { VARHAISKASVATUKSEN_TOIMIPAIKKA } from '../uusioppija/esiopetuksenSuoritus'

export const createOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  opiskeluoikeudenTyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi'>,
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste | undefined,
  alku: string,
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', any>,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  maksuton?: boolean | null,
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus'>,
  tuvaJärjestämislupa?: Koodistokoodiviite<'tuvajarjestamislupa'>,
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet'>,
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero'>,
  tpoOppimäärä?: Koodistokoodiviite<'taiteenperusopetusoppimaara'>,
  tpoTaiteenala?: Koodistokoodiviite<'taiteenperusopetustaiteenala'>,
  tpoToteutustapa?: Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
): Opiskeluoikeus | undefined => {
  switch (opiskeluoikeudenTyyppi.koodiarvo) {
    case 'perusopetus':
      if (!peruste || !suorituskieli) return undefined
      return createPerusopetuksenOpiskeluoikeus(
        suorituksenTyyppi,
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )
    case 'perusopetukseenvalmistavaopetus':
      if (!peruste || !suorituskieli) return undefined
      return createPerusopetukseenValmistavaOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )
    case 'perusopetuksenlisaopetus':
      if (!peruste || maksuton === undefined || !suorituskieli) return undefined
      return createPerusopetuksenLisäopetuksenOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli,
        maksuton
      )

    case 'aikuistenperusopetus':
      if (
        !peruste ||
        maksuton === undefined ||
        !opintojenRahoitus ||
        !suorituskieli
      ) {
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
      if (!peruste || !suorituskieli) return undefined
      return createEsiopetuksenOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli
      )

    case 'tuva':
      if (
        !peruste ||
        !tuvaJärjestämislupa ||
        !opintojenRahoitus ||
        !suorituskieli
      ) {
        return undefined
      }
      return createTutkintokoulutukseenValmentavanOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        suorituskieli,
        tuvaJärjestämislupa
      )

    case 'muukuinsaanneltykoulutus':
      if (
        !opintojenRahoitus ||
        !opintokokonaisuus ||
        !jotpaAsianumero ||
        !suorituskieli
      ) {
        return undefined
      }
      return createMuunKuinSäännellynKoulutuksenOpiskeluoikeus(
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        suorituskieli,
        opintokokonaisuus,
        jotpaAsianumero
      )

    case 'taiteenperusopetus':
      if (!peruste || !tpoOppimäärä || !tpoTaiteenala || !tpoToteutustapa) {
        return undefined
      }
      return createTaiteenPerusopetuksenOpiskeluoikeus(
        suorituksenTyyppi,
        peruste,
        organisaatio,
        alku,
        tila,
        tpoOppimäärä,
        tpoTaiteenala,
        tpoToteutustapa
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

// Esiopetus
const createEsiopetuksenOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>
) =>
  EsiopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: NuortenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        NuortenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
      ]
    }),
    suoritukset: [
      EsiopetuksenSuoritus({
        suorituskieli,
        koulutusmoduuli: Esiopetus({
          perusteenDiaarinumero: peruste.koodiarvo,
          tunniste: Koodistokoodiviite({
            koodiarvo: organisaatio.oppilaitostyyppi?.includes(
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

// Tutkintokoulutukseen valmentava koulutus

const createTutkintokoulutukseenValmentavanOpiskeluoikeus = (
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

// Muu kuin säännelty koulutus

const createMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet'>,
  jotpaAsianumero: Koodistokoodiviite<'jotpaasianumero'>
) =>
  MuunKuinSäännellynKoulutuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: MuunKuinSäännellynKoulutuksenTila({
      opiskeluoikeusjaksot: [
        MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso({
          alku,
          tila,
          opintojenRahoitus
        })
      ]
    }),
    lisätiedot: MuunKuinSäännellynKoulutuksenLisätiedot({
      jotpaAsianumero
    }),
    suoritukset: [
      MuunKuinSäännellynKoulutuksenPäätasonSuoritus({
        suorituskieli,
        koulutusmoduuli: MuuKuinSäänneltyKoulutus({
          opintokokonaisuus: opintokokonaisuus
        }),
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })

// Taiteen perusopetus

const createTaiteenPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: TaiteenPerusopetuksenOpiskeluoikeusjakso['tila'],
  oppimäärä: Koodistokoodiviite<'taiteenperusopetusoppimaara'>,
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala'>,
  koulutuksenToteutustapa: Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
) =>
  TaiteenPerusopetuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    oppimäärä,
    koulutuksenToteutustapa,
    tila: TaiteenPerusopetuksenOpiskeluoikeudenTila({
      opiskeluoikeusjaksot: [
        TaiteenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
      ]
    }),
    suoritukset: [
      createTaiteenPerusopetuksenPäätasonSuoritus(
        suorituksenTyyppi,
        taiteenala,
        peruste,
        organisaatio
      )
    ]
  })

const createTaiteenPerusopetuksenPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia
): TaiteenPerusopetuksenPäätasonSuoritus => {
  const suoritusCtors = {
    taiteenperusopetuksenlaajanoppimaaranperusopinnot:
      TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus,
    taiteenperusopetuksenlaajanoppimaaransyventavatopinnot:
      TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus,
    taiteenperusopetuksenyleisenoppimaaranteemaopinnot:
      TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus,
    taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot:
      TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus
  }

  const createOpintotaso =
    suoritusCtors[suorituksenTyyppi.koodiarvo as keyof typeof suoritusCtors]

  return createOpintotaso({
    koulutusmoduuli: createTaiteenPerusopetuksenOpintotaso(taiteenala, peruste),
    toimipiste: toToimipiste(organisaatio)
  })
}

const createTaiteenPerusopetuksenOpintotaso = (
  taiteenala: Koodistokoodiviite<'taiteenperusopetustaiteenala'>,
  peruste: Peruste
): TaiteenPerusopetuksenOpintotaso => {
  const taiteenalaCtors = {
    arkkitehtuuri: ArkkitehtuurinOpintotaso,
    kasityo: KäsityönOpintotaso,
    kuvataide: KuvataiteenOpintotaso,
    mediataiteet: MediataiteenOpintotaso,
    musiikki: MusiikinOpintotaso,
    sanataide: SanataiteenOpintotaso,
    sirkustaide: SirkustaiteenOpintotaso,
    tanssi: TanssinOpintotaso,
    teatteritaide: TeatteritaiteenOpintotaso
  }
  const createOpintotaso =
    taiteenalaCtors[taiteenala.koodiarvo as keyof typeof taiteenalaCtors]
  return createOpintotaso({ perusteenDiaarinumero: peruste.koodiarvo })
}
