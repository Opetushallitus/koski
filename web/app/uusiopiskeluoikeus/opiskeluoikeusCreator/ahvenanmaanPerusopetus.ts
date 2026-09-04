import { ahvenanmaanPäättötodistuksenOppiaineet } from '../../ahvenanmaan-perusopetus/ahvenanmaanLuokkaAsteenOppiaineet'
import { AHVENANMAAN_PERUSOPETUKSEN_DIAARINUMERO } from '../../ahvenanmaan-perusopetus/ahvenanmaanPeruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import { AhvenanmaanPerusopetus } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeudenTila'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeusjakso'
import { AhvenanmaanPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppimaaranSuoritus'
import { AhvenanmaanPerusopetuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenPaatasonSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { toOppilaitos, toToimipiste } from './utils'

// Ahvenanmaan perusopetus. Vuosiluokan suorituksia ei luoda tässä dialogissa
// vaan editorissa (UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal),
// samoin kuin manner-Suomen perusopetuksessa.
export const createAhvenanmaanPerusopetuksenOpiskeluoikeus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: AhvenanmaanPerusopetuksenOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'> | undefined
) => {
  if (!suorituskieli) return undefined

  const suoritus = createAhvenanmaanPerusopetuksenSuoritus(
    suorituksenTyyppi,
    organisaatio,
    alku,
    suorituskieli
  )

  return (
    suoritus &&
    AhvenanmaanPerusopetuksenOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: AhvenanmaanPerusopetuksenOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          AhvenanmaanPerusopetuksenOpiskeluoikeusjakso({ alku, tila })
        ]
      }),
      suoritukset: [suoritus]
    })
  )
}

const createAhvenanmaanPerusopetuksenSuoritus = (
  suorituksenTyyppi: Koodistokoodiviite<'suorituksentyyppi'>,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  suorituskieli: Koodistokoodiviite<'kieli'>
): AhvenanmaanPerusopetuksenPäätasonSuoritus | undefined => {
  // Ops ei ole ePerusteissa, joten diaarinumeroa ei valita dialogissa vaan se
  // kirjataan vakiona. Esitäytettävät oppiaineet tulevat samasta lähteestä kuin
  // editorin vuosiluokkasuorituksilla, ei backendin prefill-rajapinnasta.
  const yhteiset = {
    koulutusmoduuli: AhvenanmaanPerusopetus({
      perusteenDiaarinumero: AHVENANMAAN_PERUSOPETUKSEN_DIAARINUMERO
    }),
    toimipiste: toToimipiste(organisaatio),
    suorituskieli,
    suoritustapa: Koodistokoodiviite({
      koodiarvo: 'koulutus',
      koodistoUri: 'perusopetuksensuoritustapa'
    }),
    osasuoritukset: ahvenanmaanPäättötodistuksenOppiaineet(
      AHVENANMAAN_PERUSOPETUKSEN_DIAARINUMERO
    )
  }

  switch (suorituksenTyyppi.koodiarvo) {
    case 'ahvenanmaanperusopetuksenoppimaara':
      return AhvenanmaanPerusopetuksenOppimääränSuoritus(yhteiset)
    case 'ahvenanmaanperusopetuksenoppimaaraaikuiset':
      // Muiden kuin oppivelvollisten opiskeluoikeudella ei ole vuosiluokan
      // suorituksia, joten alkamispäivä kirjataan oppimäärän suoritukselle
      // (ks. KoskiValidator.validateAlkamispäivä).
      return AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus({
        ...yhteiset,
        alkamispäivä: alku
      })
    default:
      return undefined
  }
}
