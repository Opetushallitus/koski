import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AikuistenPerusopetuksenÄidinkieliJaKirjallisuus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenAidinkieliJaKirjallisuus'
import { AikuistenPerusopetuksenAlkuvaihe } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenAlkuvaihe'
import { AikuistenPerusopetuksenAlkuvaiheenSuoritus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenAlkuvaiheenSuoritus'
import { AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AikuistenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeudenTila'
import { AikuistenPerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeus'
import { AikuistenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOpiskeluoikeusjakso'
import { AikuistenPerusopetuksenOppiaineenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { AikuistenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenOppimaaranSuoritus'
import { AikuistenPerusopetuksenUskonto } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenUskonto'
import { AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli } from '../../types/fi/oph/koski/schema/AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
import { AikuistenPerusopetus } from '../../types/fi/oph/koski/schema/AikuistenPerusopetus'
import { EiTiedossaOppiaine } from '../../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuAikuistenPerusopetuksenOppiaine } from '../../types/fi/oph/koski/schema/MuuAikuistenPerusopetuksenOppiaine'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import {
  isEiTiedossaOlevaOppiaine,
  isUskonnonOppiaine,
  isVieraanKielenOppiaine,
  isÄidinkielenOppiaine
} from './perusopetus'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

// Aikuisten perusopetus
export const createAikuistenPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton: boolean | null,
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  const suoritus = createSuoritus(
    suorituksenTyyppi,
    peruste,
    organisaatio,
    suorituskieli,
    oppiaine,
    kieliaineenKieli,
    äidinkielenKieli
  )

  return (
    suoritus &&
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
      suoritukset: [suoritus],
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
      )
    })
  )
}

const createSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'aikuistenperusopetuksenoppimaara': {
      return AikuistenPerusopetuksenOppimääränSuoritus({
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
    }
    case 'perusopetuksenoppiaineenoppimaara': {
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
        AikuistenPerusopetuksenOppiaineenOppimääränSuoritus({
          koulutusmoduuli,
          suorituskieli,
          suoritustapa: Koodistokoodiviite({
            koodiarvo: 'koulutus',
            koodistoUri: 'perusopetuksensuoritustapa'
          }),
          toimipiste: toToimipiste(organisaatio)
        })
      )
    }
    case 'aikuistenperusopetuksenoppimaaranalkuvaihe':
      return AikuistenPerusopetuksenAlkuvaiheenSuoritus({
        koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe({
          perusteenDiaarinumero: peruste.koodiarvo
        }),
        suorituskieli,
        suoritustapa: Koodistokoodiviite({
          koodiarvo: 'koulutus',
          koodistoUri: 'perusopetuksensuoritustapa'
        }),
        toimipiste: toToimipiste(organisaatio)
      })
    default:
      return undefined
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
  } else if (isUskonnonOppiaine(oppiaine.koodiarvo)) {
    return AikuistenPerusopetuksenUskonto({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false // ???
    })
  } else if (isVieraanKielenOppiaine(oppiaine.koodiarvo)) {
    return (
      kieliaineenKieli &&
      AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false, // ???
        kieli: kieliaineenKieli
      })
    )
  } else if (isÄidinkielenOppiaine(oppiaine.koodiarvo)) {
    return (
      äidinkielenKieli &&
      AikuistenPerusopetuksenÄidinkieliJaKirjallisuus({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false, // ???
        kieli: äidinkielenKieli
      })
    )
  } else {
    return MuuAikuistenPerusopetuksenOppiaine({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false // ???
    })
  }
}
