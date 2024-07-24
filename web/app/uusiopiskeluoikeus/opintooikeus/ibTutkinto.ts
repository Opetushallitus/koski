import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { DIAOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/DIAOpiskeluoikeusjakso'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenLisatiedot'
import { LukionOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { LukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { PreIBSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2015'
import { PreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2019'
import { SuoritusClass } from '../../types/fi/oph/koski/typemodel/SuoritusClass'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createIBOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: DIAOpiskeluoikeusjakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  maksuton: boolean | null
) => {
  const suoritus = createIBPäätasonSuoritus(
    suorituksenTyyppi,
    suorituskieli,
    organisaatio
  )

  return (
    suoritus &&
    IBOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: LukionOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          LukionOpiskeluoikeusjakso({ alku, tila, opintojenRahoitus })
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

const createIBPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  organisaatio: OrganisaatioHierarkia
) => {
  console.log('ib suorituksen tyyppi', suorituksenTyyppi)
  switch (suorituksenTyyppi.koodiarvo) {
    case 'ibtutkinto':
      return IBTutkinnonSuoritus({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case 'preiboppimaara':
      return PreIBSuoritus2015({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    case UIPreIb2019PäätuorituksenTyyppi:
      return PreIBSuoritus2019({
        suorituskieli,
        toimipiste: toToimipiste(organisaatio)
      })
    default:
      return undefined
  }
}

// Hack, joka tarvitaan koska 2015- ja 2019-mallisilla Pre-IB-suorituksilla on sama suorituksen tyyppi.
// Käyttöliittymässä leikitään että uudemman suorituksen tyyppi olisi 'preiboppimaara2019',
// mutta se mäpätään takaisin oikeaksi opiskeluoikeutta luotaessa.
export const UIPreIb2019PäätuorituksenTyyppi = 'preiboppimaara2019'
export const hackSuoritusMappingForPreIB2019 = (
  cs: SuoritusClass[]
): SuoritusClass[] =>
  cs.map((cn) =>
    cn.className === PreIBSuoritus2019.className
      ? { ...cn, tyyppi: UIPreIb2019PäätuorituksenTyyppi }
      : cn
  )
