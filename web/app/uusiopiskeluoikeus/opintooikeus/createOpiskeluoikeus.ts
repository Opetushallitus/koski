import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { createAikuistenPerusopetuksenOpiskeluoikeus } from './createAikuistenPerusopetuksenOpiskeluoikeus'
import { createEsiopetuksenOpiskeluoikeus } from './createEsiopetuksenOpiskeluoikeus'
import { createMuunKuinSäännellynKoulutuksenOpiskeluoikeus } from './createMuunKuinSäännellynKoulutuksenOpiskeluoikeus'
import { createPerusopetukseenValmistavaOpiskeluoikeus } from './createPerusopetukseenValmistavaOpiskeluoikeus'
import { createPerusopetuksenLisäopetuksenOpiskeluoikeus } from './createPerusopetuksenLisäopetuksenOpiskeluoikeus'
import { createPerusopetuksenOpiskeluoikeus } from './createPerusopetuksenOpiskeluoikeus'
import { createTaiteenPerusopetuksenOpiskeluoikeus } from './createTaiteenPerusopetuksenOpiskeluoikeus'
import { createTutkintokoulutukseenValmentavanOpiskeluoikeus } from './createTutkintokoulutukseenValmentavanOpiskeluoikeus'

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
  tpoToteutustapa?: Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>,
  varhaiskasvatuksenJärjestämismuoto?: Koodistokoodiviite<'vardajarjestamismuoto'>
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
        suorituskieli,
        varhaiskasvatuksenJärjestämismuoto
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
