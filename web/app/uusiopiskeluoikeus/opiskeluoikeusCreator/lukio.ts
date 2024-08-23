import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EiTiedossaOppiaine } from '../../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionÄidinkieliJaKirjallisuus2015 } from '../../types/fi/oph/koski/schema/LukionAidinkieliJaKirjallisuus2015'
import { LukionMatematiikka2015 } from '../../types/fi/oph/koski/schema/LukionMatematiikka2015'
import { LukionMuuValtakunnallinenOppiaine2015 } from '../../types/fi/oph/koski/schema/LukionMuuValtakunnallinenOppiaine2015'
import { LukionOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenLisatiedot'
import { LukionOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { LukionOpiskeluoikeus } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeus'
import { LukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { LukionOppiaineenOppimääränSuoritus2015 } from '../../types/fi/oph/koski/schema/LukionOppiaineenOppimaaranSuoritus2015'
import { LukionOppiaineidenOppimäärät2019 } from '../../types/fi/oph/koski/schema/LukionOppiaineidenOppimaarat2019'
import { LukionOppiaineidenOppimäärienSuoritus2019 } from '../../types/fi/oph/koski/schema/LukionOppiaineidenOppimaarienSuoritus2019'
import { LukionOppimäärä } from '../../types/fi/oph/koski/schema/LukionOppimaara'
import { LukionOppimääränSuoritus2015 } from '../../types/fi/oph/koski/schema/LukionOppimaaranSuoritus2015'
import { LukionOppimääränSuoritus2019 } from '../../types/fi/oph/koski/schema/LukionOppimaaranSuoritus2019'
import { LukionPäätasonSuoritus } from '../../types/fi/oph/koski/schema/LukionPaatasonSuoritus'
import { LukionUskonto2015 } from '../../types/fi/oph/koski/schema/LukionUskonto2015'
import { VierasTaiToinenKotimainenKieli2015 } from '../../types/fi/oph/koski/schema/VierasTaiToinenKotimainenKieli2015'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'
import {
  isEiTiedossaOlevaOppiaine,
  isMatematiikanOppiaine,
  isUskonnonOppiaine,
  isVieraanKielenOppiaine,
  isÄidinkielenOppiaine
} from './yleissivistavat'

export const lukionDiaarinumerot2019 = ['OPH-2263-2019', 'OPH-2267-2019']

export const createLukionOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: LukionOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  maksuton: boolean | null,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava', any>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  const suoritus = createLukionPäätasonSuoritus(
    suorituksenTyyppi,
    peruste,
    organisaatio,
    suorituskieli,
    oppiaine,
    kieliaineenKieli,
    äidinkielenKieli
  )
  const oppimäärä = perusteToOppimäärä(peruste)

  return (
    suoritus &&
    LukionOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: LukionOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          LukionOpiskeluoikeusjakso({
            alku,
            tila,
            opintojenRahoitus
          })
        ]
      }),
      suoritukset: [suoritus],
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        LukionOpiskeluoikeudenLisätiedot
      )
    })
  )
}

const createLukionPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava', any>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
): LukionPäätasonSuoritus | undefined => {
  const oppimäärä = perusteToOppimäärä(peruste)

  if (!oppimäärä) return undefined

  switch (suorituksenTyyppi.koodiarvo) {
    case 'lukionaineopinnot':
      return LukionOppiaineidenOppimäärienSuoritus2019({
        koulutusmoduuli: LukionOppiaineidenOppimäärät2019({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        oppimäärä,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'lukionoppimaara': {
      const createSuoritus = lukionDiaarinumerot2019.includes(peruste.koodiarvo)
        ? LukionOppimääränSuoritus2019
        : LukionOppimääränSuoritus2015

      return createSuoritus({
        koulutusmoduuli: LukionOppimäärä({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        oppimäärä,
        toimipiste: toToimipiste(organisaatio)
      })
    }
    case 'lukionoppiaineenoppimaara': {
      const koulutusmoduuli =
        oppiaine &&
        createOppiaineenKoulutusmoduuli(
          oppiaine,
          peruste,
          kieliaineenKieli,
          äidinkielenKieli
        )
      return (
        koulutusmoduuli &&
        LukionOppiaineenOppimääränSuoritus2015({
          suorituskieli,
          koulutusmoduuli,
          toimipiste: toToimipiste(organisaatio)
        })
      )
    }
    default:
      return undefined
  }
}

export const perusteToOppimäärä = (
  peruste: Peruste
): Koodistokoodiviite<'lukionoppimaara'> | undefined => {
  switch (peruste.koodiarvo) {
    case '60/011/2015':
    case '33/011/2003':
    case 'OPH-2263-2019':
    case '56/011/2015':
    case 'OPH-4958-2020':
      return Koodistokoodiviite({
        koodiarvo: 'nuortenops',
        koodistoUri: 'lukionoppimaara'
      })
    case '70/011/2015':
    case '4/011/2004':
    case 'OPH-2267-2019':
      return Koodistokoodiviite({
        koodiarvo: 'aikuistenops',
        koodistoUri: 'lukionoppimaara'
      })
  }
}

const createOppiaineenKoulutusmoduuli = (
  oppiaine: Koodistokoodiviite<'koskioppiaineetyleissivistava', any>,
  peruste: Peruste,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  if (isEiTiedossaOlevaOppiaine(oppiaine.koodiarvo)) {
    return EiTiedossaOppiaine({
      perusteenDiaarinumero: peruste.koodiarvo
    })
  } else if (isMatematiikanOppiaine(oppiaine.koodiarvo)) {
    return LukionMatematiikka2015({
      pakollinen: false,
      perusteenDiaarinumero: peruste.koodiarvo,
      oppimäärä: Koodistokoodiviite({
        koodiarvo: 'MAA', // TODO: lisää kenttä tämän valintaan
        koodistoUri: 'oppiainematematiikka'
      })
    })
  } else if (isUskonnonOppiaine(oppiaine.koodiarvo)) {
    return LukionUskonto2015({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false
    })
  } else if (isVieraanKielenOppiaine(oppiaine.koodiarvo)) {
    return (
      kieliaineenKieli &&
      VierasTaiToinenKotimainenKieli2015({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false, // ???
        kieli: kieliaineenKieli
      })
    )
  } else if (isÄidinkielenOppiaine(oppiaine.koodiarvo)) {
    return (
      äidinkielenKieli &&
      LukionÄidinkieliJaKirjallisuus2015({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false,
        kieli: äidinkielenKieli
      })
    )
  } else {
    return LukionMuuValtakunnallinenOppiaine2015({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false
    })
  }
}
