import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { EiTiedossaOppiaine } from '../../types/fi/oph/koski/schema/EiTiedossaOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuNuortenPerusopetuksenOppiaine } from '../../types/fi/oph/koski/schema/MuuNuortenPerusopetuksenOppiaine'
import { NuortenPerusopetuksenÄidinkieliJaKirjallisuus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenAidinkieliJaKirjallisuus'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeudenTila'
import { NuortenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOpiskeluoikeusjakso'
import { NuortenPerusopetuksenOppiaineenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import { NuortenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { NuortenPerusopetuksenUskonto } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenUskonto'
import { NuortenPerusopetuksenVierasTaiToinenKotimainenKieli } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
import { NuortenPerusopetus } from '../../types/fi/oph/koski/schema/NuortenPerusopetus'
import { PerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import { toOppilaitos, toToimipiste } from './utils'

// Perusopetus
export const createPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: NuortenPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  const suoritus = createPerusopetuksenSuoritus(
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
    PerusopetuksenOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: NuortenPerusopetuksenOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          NuortenPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
        ]
      }),
      suoritukset: [suoritus]
    })
  )
}

const createPerusopetuksenSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  oppiaine?: Koodistokoodiviite<'koskioppiaineetyleissivistava'>,
  kieliaineenKieli?: Koodistokoodiviite<'kielivalikoima'>,
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
) => {
  switch (suorituksenTyyppi.koodiarvo) {
    case 'perusopetuksenoppimaara': {
      return NuortenPerusopetuksenOppimääränSuoritus({
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
    }
    case 'nuortenperusopetuksenoppiaineenoppimaara': {
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
        NuortenPerusopetuksenOppiaineenOppimääränSuoritus({
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
    return NuortenPerusopetuksenUskonto({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false // ???
    })
  } else if (isVieraanKielenOppiaine(oppiaine.koodiarvo)) {
    return (
      kieliaineenKieli &&
      NuortenPerusopetuksenVierasTaiToinenKotimainenKieli({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false, // ???
        kieli: kieliaineenKieli
      })
    )
  } else if (isÄidinkielenOppiaine(oppiaine.koodiarvo)) {
    return (
      äidinkielenKieli &&
      NuortenPerusopetuksenÄidinkieliJaKirjallisuus({
        tunniste: oppiaine,
        perusteenDiaarinumero: peruste.koodiarvo,
        pakollinen: false, // ???
        kieli: äidinkielenKieli
      })
    )
  } else {
    return MuuNuortenPerusopetuksenOppiaine({
      tunniste: oppiaine,
      perusteenDiaarinumero: peruste.koodiarvo,
      pakollinen: false // ???
    })
  }
}

const contains =
  (...as: string[]) =>
  (a?: string): boolean =>
    a !== undefined && as.includes(a)

export const isUskonnonOppiaine = contains('KT')
export const isVieraanKielenOppiaine = contains(
  'A1',
  'A2',
  'B1',
  'B2',
  'B3',
  'AOM'
)
export const isÄidinkielenOppiaine = contains('AI')
export const isEiTiedossaOlevaOppiaine = contains('XX')
export const isMuuOppiaine = (koodiarvo: string) =>
  !isUskonnonOppiaine(koodiarvo) &&
  !isVieraanKielenOppiaine(koodiarvo) &&
  !isÄidinkielenOppiaine(koodiarvo) &&
  !isEiTiedossaOlevaOppiaine(koodiarvo)
