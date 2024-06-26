import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { ArkkitehtuurinOpintotaso } from '../../types/fi/oph/koski/schema/ArkkitehtuurinOpintotaso'
import { KäsityönOpintotaso } from '../../types/fi/oph/koski/schema/KasityonOpintotaso'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KuvataiteenOpintotaso } from '../../types/fi/oph/koski/schema/KuvataiteenOpintotaso'
import { MediataiteenOpintotaso } from '../../types/fi/oph/koski/schema/MediataiteenOpintotaso'
import { MusiikinOpintotaso } from '../../types/fi/oph/koski/schema/MusiikinOpintotaso'
import { SanataiteenOpintotaso } from '../../types/fi/oph/koski/schema/SanataiteenOpintotaso'
import { SirkustaiteenOpintotaso } from '../../types/fi/oph/koski/schema/SirkustaiteenOpintotaso'
import { TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranPerusopintojenSuoritus'
import { TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenLaajanOppimaaranSyventavienOpintojenSuoritus'
import { TaiteenPerusopetuksenOpintotaso } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpintotaso'
import { TaiteenPerusopetuksenOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeudenTila'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeusjakso'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { TanssinOpintotaso } from '../../types/fi/oph/koski/schema/TanssinOpintotaso'
import { TeatteritaiteenOpintotaso } from '../../types/fi/oph/koski/schema/TeatteritaiteenOpintotaso'
import { toOppilaitos, toToimipiste } from './utils'

// Taiteen perusopetus
export const createTaiteenPerusopetuksenOpiskeluoikeus = (
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

export const createTaiteenPerusopetuksenPäätasonSuoritus = (
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
