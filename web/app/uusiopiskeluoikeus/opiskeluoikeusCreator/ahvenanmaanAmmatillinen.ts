import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AhvenanmaanAmmatillinenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeudenTila'
import { AhvenanmaanAmmatillinenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeus'
import { AhvenanmaanAmmatillinenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenOpiskeluoikeusjakso'
import { AhvenanmaanAmmatillinenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillinenPaatasonSuoritus'
import { AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus'
import { AhvenanmaanAmmatillisenTutkinnonSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanAmmatillisenTutkinnonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { createAmmatillinenTutkintoKoulutus } from './ammatillinenTutkinto'
import { toOppilaitos, toToimipiste } from './utils'

// Suoritustapa on aina reformi (suoritusluokkien oletusarvo), eikä
// opiskeluoikeusjaksoilla ole rahoitustietoa.
export const createAhvenanmaanAmmatillinenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: AhvenanmaanAmmatillinenOpiskeluoikeusjakso['tila'],
  suorituskieli?: Koodistokoodiviite<'kieli'>,
  tutkinto?: TutkintoPeruste
): AhvenanmaanAmmatillinenOpiskeluoikeus | undefined => {
  if (!suorituskieli || !tutkinto) return undefined

  const suoritus = createPäätasonSuoritus(
    suorituksenTyyppi,
    organisaatio,
    suorituskieli,
    tutkinto
  )

  return (
    suoritus &&
    AhvenanmaanAmmatillinenOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: AhvenanmaanAmmatillinenOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          AhvenanmaanAmmatillinenOpiskeluoikeusjakso({ alku, tila })
        ]
      }),
      suoritukset: [suoritus]
    })
  )
}

const createPäätasonSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  tutkinto: TutkintoPeruste
): AhvenanmaanAmmatillinenPäätasonSuoritus | undefined => {
  const yhteiset = {
    koulutusmoduuli: createAmmatillinenTutkintoKoulutus(tutkinto),
    suorituskieli,
    toimipiste: toToimipiste(organisaatio)
  }
  switch (suorituksenTyyppi.koodiarvo) {
    case 'ahvenanmaanammatillinentutkinto':
      return AhvenanmaanAmmatillisenTutkinnonSuoritus(yhteiset)
    case 'ahvenanmaanammatillinentutkintoosittainen':
      return AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus({
        ...yhteiset,
        toinenTutkintonimike: false,
        toinenOsaamisala: false
      })
    default:
      return undefined
  }
}
