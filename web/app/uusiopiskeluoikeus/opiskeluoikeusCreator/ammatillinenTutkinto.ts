import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AmmatillinenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeudenTila'
import { AmmatillinenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { AmmatillinenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { AmmatillinenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/AmmatillinenPaatasonSuoritus'
import { AmmatillinenTutkintoKoulutus } from '../../types/fi/oph/koski/schema/AmmatillinenTutkintoKoulutus'
import { AmmatilliseenTehtäväänValmistavaKoulutus } from '../../types/fi/oph/koski/schema/AmmatilliseenTehtavaanValmistavaKoulutus'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/AmmatillisenOpiskeluoikeudenLisatiedot'
import { AmmatillisenTutkinnonOsittainenSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'
import { AmmatillisenTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonSuoritus'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuAmmatillinenKoulutus } from '../../types/fi/oph/koski/schema/MuuAmmatillinenKoulutus'
import { MuunAmmatillisenKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/MuunAmmatillisenKoulutuksenSuoritus'
import { NäyttötutkintoonValmistavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/NayttotutkintoonValmistavanKoulutuksenSuoritus'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenMuuAmmatillinenKoulutus } from '../../types/fi/oph/koski/schema/PaikallinenMuuAmmatillinenKoulutus'
import { TelmaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/TelmaKoulutuksenSuoritus'
import { TelmaKoulutus } from '../../types/fi/oph/koski/schema/TelmaKoulutus'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus } from '../../types/fi/oph/koski/schema/TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaKoulutus'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus } from '../../types/fi/oph/koski/schema/TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaSuoritus'
import { ValmaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/ValmaKoulutuksenSuoritus'
import { ValmaKoulutus } from '../../types/fi/oph/koski/schema/ValmaKoulutus'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createAmmatillinenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  suorituskieli: Koodistokoodiviite<'kieli'> | undefined,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', any>,
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus'>,
  maksuton?: boolean | null,
  muuAmmatillinenKoulutus?: MuuAmmatillinenKoulutus,
  suoritustapa?: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>,
  tutkinto?: TutkintoPeruste,
  peruste?: Peruste,
  tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus?: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
) => {
  if (!suorituskieli || !opintojenRahoitus || maksuton === undefined) {
    return undefined
  }

  const suoritus = createAmmatillinenPäätasonSuoritus(
    suorituksenTyyppi,
    suorituskieli,
    organisaatio,
    muuAmmatillinenKoulutus,
    suoritustapa,
    tutkinto,
    peruste,
    tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
  )

  return (
    suoritus &&
    AmmatillinenOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: AmmatillinenOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          AmmatillinenOpiskeluoikeusjakso({
            alku,
            tila,
            opintojenRahoitus
          })
        ]
      }),
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        AmmatillisenOpiskeluoikeudenLisätiedot
      ),
      suoritukset: [suoritus]
    })
  )
}

const createAmmatillinenPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  organisaatio: OrganisaatioHierarkia,
  muuAmmatillinenKoulutus?: MuuAmmatillinenKoulutus,
  suoritustapa?: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>,
  tutkinto?: TutkintoPeruste,
  peruste?: Peruste,
  tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus?: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
): AmmatillinenPäätasonSuoritus | undefined => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'ammatillinentutkintoosittainen':
      if (!tutkinto || !suoritustapa) return undefined
      return AmmatillisenTutkinnonOsittainenSuoritus({
        koulutusmoduuli: createAmmatillinenTutkintoKoulutus(tutkinto),
        suorituskieli,
        suoritustapa,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'ammatillinentutkinto':
      if (!tutkinto || !suoritustapa) return undefined
      return AmmatillisenTutkinnonSuoritus({
        koulutusmoduuli: createAmmatillinenTutkintoKoulutus(tutkinto),
        suorituskieli,
        suoritustapa,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'muuammatillinenkoulutus':
      if (!muuAmmatillinenKoulutus) return undefined
      return MuunAmmatillisenKoulutuksenSuoritus({
        koulutusmoduuli: muuAmmatillinenKoulutus,
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'nayttotutkintoonvalmistavakoulutus':
      if (!tutkinto) return undefined
      return NäyttötutkintoonValmistavanKoulutuksenSuoritus({
        tutkinto: createAmmatillinenTutkintoKoulutus(tutkinto),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'telma':
      if (!peruste) return undefined
      return TelmaKoulutuksenSuoritus({
        koulutusmoduuli: TelmaKoulutus({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus':
      if (!tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus)
        return undefined
      return TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus({
        koulutusmoduuli:
          tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'valma':
      if (!peruste) return undefined
      return ValmaKoulutuksenSuoritus({
        koulutusmoduuli: ValmaKoulutus({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    default:
      return undefined
  }
}

const createAmmatillinenTutkintoKoulutus = (tutkinto: TutkintoPeruste) =>
  AmmatillinenTutkintoKoulutus({
    perusteenDiaarinumero: tutkinto.diaarinumero,
    tunniste: Koodistokoodiviite({
      koodiarvo: tutkinto.tutkintoKoodi,
      koodistoUri: 'koulutus'
    })
  })

export const createPaikallinenMuuAmmatillinenKoulutus = (
  nimi: string,
  koodiarvo: string,
  kuvaus: string
) =>
  PaikallinenMuuAmmatillinenKoulutus({
    tunniste: PaikallinenKoodi({
      koodiarvo,
      nimi: Finnish({ fi: nimi })
    }),
    kuvaus: Finnish({ fi: kuvaus })
  })

export const createAmmatilliseenTehtäväänValmistavaKoulutus = (
  tunniste: Koodistokoodiviite<'ammatilliseentehtavaanvalmistavakoulutus'>
) => AmmatilliseenTehtäväänValmistavaKoulutus({ tunniste })

export const createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = (
  nimi: string,
  koodiarvo: string,
  kuvaus: string
) =>
  TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus({
    tunniste: PaikallinenKoodi({
      koodiarvo,
      nimi: Finnish({ fi: nimi })
    }),
    kuvaus: Finnish({ fi: kuvaus })
  })
