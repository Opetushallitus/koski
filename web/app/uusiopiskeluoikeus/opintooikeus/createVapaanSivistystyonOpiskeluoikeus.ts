import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Maksuttomuus } from '../../types/fi/oph/koski/schema/Maksuttomuus'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VSTKotoutumiskoulutus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutus2022'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönJotpaKoulutus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutus'
import { VapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { VapaanSivistystyönOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import { VapaanSivistystyönOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeudenTila'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönOsaamismerkinSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import { VapaanSivistystyönOsaamismerkki } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkki'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { VapaanSivistystyönVapaatavoitteinenKoulutus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteinenKoulutus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { toOppilaitos, toToimipiste } from './utils'

export const createVapaanSivistystyönOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  alku: string,
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', any>,
  peruste?: Peruste,
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet'>,
  osaamismerkki?: Koodistokoodiviite<'osaamismerkit'>,
  maksuton?: boolean | null,
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero'>
) => {
  const suoritus = createVapaanSivistystyönPäätasonSuoritus(
    suorituksenTyyppi,
    organisaatio,
    peruste,
    suorituskieli,
    opintokokonaisuus,
    osaamismerkki
  )

  const ooTila = createVapaanSivistystyönOpiskeluoikeudenTila(
    suorituksenTyyppi,
    alku,
    tila,
    opintojenRahoitus
  )

  const lisätiedot = createVapaanSivistystyönOpiskeluoikeudenLisätiedot(
    suorituksenTyyppi,
    alku,
    maksuton,
    jotpaAsianumero
  )

  return (
    suoritus &&
    ooTila &&
    lisätiedot &&
    VapaanSivistystyönOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: ooTila,
      suoritukset: [suoritus],
      lisätiedot
    })
  )
}

const createVapaanSivistystyönOpiskeluoikeudenTila = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  alku: string,
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', any>,
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', any>
): VapaanSivistystyönOpiskeluoikeudenTila | undefined => {
  const jakso = (() => {
    switch (suorituksenTyyppi.koodiarvo) {
      case 'vstoppivelvollisillesuunnattukoulutus':
      case 'vstmaahanmuuttajienkotoutumiskoulutus':
      case 'vstlukutaitokoulutus':
        return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso({
          alku,
          tila
        })
      case 'vstjotpakoulutus':
        return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso({
          alku,
          tila,
          opintojenRahoitus
        })
      case 'vstosaamismerkki':
        return VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso({
          alku,
          tila
        })
      case 'vstvapaatavoitteinenkoulutus':
        return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(
          {
            alku,
            tila
          }
        )
      default:
        return undefined
    }
  })()

  return VapaanSivistystyönOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: jakso ? [jakso] : []
  })
}

const createVapaanSivistystyönPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  peruste?: Peruste,
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet'>,
  osaamismerkki?: Koodistokoodiviite<'osaamismerkit'>
): VapaanSivistystyönPäätasonSuoritus | undefined => {
  const toimipiste = toToimipiste(organisaatio)
  const perusteenDiaarinumero = peruste?.koodiarvo

  switch (suorituksenTyyppi.koodiarvo) {
    case 'vstoppivelvollisillesuunnattukoulutus':
      if (!suorituskieli) return undefined
      return OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus({
        suorituskieli,
        toimipiste,
        koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus({
          perusteenDiaarinumero
        })
      })
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
      if (!suorituskieli) return undefined
      switch (perusteenDiaarinumero) {
        case 'OPH-649-2022':
          return OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
            {
              suorituskieli,
              toimipiste,
              koulutusmoduuli: VSTKotoutumiskoulutus2022({
                perusteenDiaarinumero
              })
            }
          )
        case 'OPH-123-2021':
        case '1/011/2012':
          return OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
            {
              suorituskieli,
              toimipiste,
              koulutusmoduuli:
                VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus({
                  perusteenDiaarinumero
                })
            }
          )
        default:
          return undefined
      }
    case 'vstlukutaitokoulutus':
      if (!suorituskieli) return undefined
      return VapaanSivistystyönLukutaitokoulutuksenSuoritus({
        suorituskieli,
        toimipiste,
        koulutusmoduuli: VapaanSivistystyönLukutaitokoulutus({
          perusteenDiaarinumero
        })
      })
    case 'vstjotpakoulutus':
      if (!suorituskieli || !opintokokonaisuus) return undefined
      return VapaanSivistystyönJotpaKoulutuksenSuoritus({
        suorituskieli,
        toimipiste,
        koulutusmoduuli: VapaanSivistystyönJotpaKoulutus({
          opintokokonaisuus
        })
      })
    case 'vstosaamismerkki':
      if (!osaamismerkki) return undefined
      return VapaanSivistystyönOsaamismerkinSuoritus({
        toimipiste,
        koulutusmoduuli: VapaanSivistystyönOsaamismerkki({
          tunniste: osaamismerkki
        })
      })
    case 'vstvapaatavoitteinenkoulutus':
      if (!suorituskieli || !opintokokonaisuus) return undefined
      return VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus({
        suorituskieli,
        toimipiste,
        koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus({
          opintokokonaisuus
        })
      })
    default:
      return undefined
  }
}

const createVapaanSivistystyönOpiskeluoikeudenLisätiedot = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  alku: string,
  maksuton?: boolean | null,
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero'>
): VapaanSivistystyönOpiskeluoikeudenLisätiedot | undefined => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'vstoppivelvollisillesuunnattukoulutus':
    case 'vstlukutaitokoulutus':
    case 'vstmaahanmuuttajienkotoutumiskoulutus':
      if (maksuton === undefined) return undefined
      return VapaanSivistystyönOpiskeluoikeudenLisätiedot({
        maksuttomuus:
          typeof maksuton === 'boolean'
            ? [Maksuttomuus({ alku, maksuton })]
            : undefined
      })
    case 'vstjotpakoulutus':
      if (jotpaAsianumero === undefined) return undefined
      return VapaanSivistystyönOpiskeluoikeudenLisätiedot({ jotpaAsianumero })
    case 'vstosaamismerkki':
    case 'vstvapaatavoitteinenkoulutus':
      return VapaanSivistystyönOpiskeluoikeudenLisätiedot()
    default:
      return undefined
  }
}
