import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuAmmatillinenKoulutus } from '../../types/fi/oph/koski/schema/MuuAmmatillinenKoulutus'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus } from '../../types/fi/oph/koski/schema/TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaKoulutus'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { createAikuistenPerusopetuksenOpiskeluoikeus } from './createAikuistenPerusopetuksenOpiskeluoikeus'
import { createAmmatillinenOpiskeluoikeus } from './createAmmatillinenTutkintoOpiskeluoikeus'
import { createDIAOpiskeluoikeus } from './createDiaTutkintoOpiskeluoikeus'
import { createEBOpiskeluoikeus } from './createEBTutkintoOpiskeluoikeus'
import { createEsiopetuksenOpiskeluoikeus } from './createEsiopetuksenOpiskeluoikeus'
import { createEuropeanSchoolOfHelsinkiOpiskeluoikeus } from './createEuropeanSchoolOfHelsinkiOpiskeluoikeus'
import { createIBOpiskeluoikeus } from './createIBTutkintoOpiskeluoikeus'
import { createInternationalSchoolOpiskeluoikeus } from './createInternationalSchoolOpiskeluoikeus'
import { createLukionOpiskeluoikeus } from './createLukiokoulutuksenOpiskeluoikeus'
import { createLukioonValmistavanKoulutuksenOpiskeluoikeus } from './createLukioonValmistavaOpiskeluoikeus'
import { createMuunKuinSäännellynKoulutuksenOpiskeluoikeus } from './createMuunKuinSäännellynKoulutuksenOpiskeluoikeus'
import { createPerusopetukseenValmistavaOpiskeluoikeus } from './createPerusopetukseenValmistavaOpiskeluoikeus'
import { createPerusopetuksenLisäopetuksenOpiskeluoikeus } from './createPerusopetuksenLisäopetuksenOpiskeluoikeus'
import { createPerusopetuksenOpiskeluoikeus } from './createPerusopetuksenOpiskeluoikeus'
import { createTaiteenPerusopetuksenOpiskeluoikeus } from './createTaiteenPerusopetuksenOpiskeluoikeus'
import { createTutkintokoulutukseenValmentavanOpiskeluoikeus } from './createTutkintokoulutukseenValmentavanOpiskeluoikeus'
import { createVapaanSivistystyönOpiskeluoikeus } from './createVapaanSivistystyonOpiskeluoikeus'

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
  varhaiskasvatuksenJärjestämismuoto?: Koodistokoodiviite<'vardajarjestamismuoto'>,
  osaamismerkki?: Koodistokoodiviite<'osaamismerkit'>,
  tutkinto?: TutkintoPeruste,
  suoritustapa?: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>,
  muuAmmatillinenKoulutus?: MuuAmmatillinenKoulutus,
  tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus?: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
  curriculum?: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum'>,
  internationalSchoolGrade?: Koodistokoodiviite<'internationalschoolluokkaaste'>
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

    case 'vapaansivistystyonkoulutus':
      return createVapaanSivistystyönOpiskeluoikeus(
        organisaatio,
        suorituksenTyyppi,
        alku,
        tila,
        peruste,
        opintojenRahoitus,
        suorituskieli,
        opintokokonaisuus,
        osaamismerkki,
        maksuton,
        jotpaAsianumero
      )

    case 'luva':
      if (
        !peruste ||
        !suorituskieli ||
        !opintojenRahoitus ||
        maksuton === undefined
      ) {
        return undefined
      }
      return createLukioonValmistavanKoulutuksenOpiskeluoikeus(
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli,
        opintojenRahoitus,
        maksuton
      )

    case 'lukiokoulutus':
      if (
        !peruste ||
        !suorituskieli ||
        !opintojenRahoitus ||
        maksuton === undefined
      ) {
        return undefined
      }
      return createLukionOpiskeluoikeus(
        suorituksenTyyppi,
        peruste,
        organisaatio,
        alku,
        tila,
        suorituskieli,
        opintojenRahoitus,
        maksuton
      )

    case 'ammatillinenkoulutus':
      if (!suorituskieli || !opintojenRahoitus || maksuton === undefined) {
        return undefined
      }
      return createAmmatillinenOpiskeluoikeus(
        suorituksenTyyppi,
        suorituskieli,
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        maksuton,
        muuAmmatillinenKoulutus,
        suoritustapa,
        tutkinto,
        peruste,
        tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
      )

    case 'europeanschoolofhelsinki':
      if (!opintojenRahoitus || !curriculum) return undefined
      return createEuropeanSchoolOfHelsinkiOpiskeluoikeus(
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        curriculum
      )

    case 'ebtutkinto':
      if (!curriculum) return undefined
      return createEBOpiskeluoikeus(organisaatio, alku, tila, curriculum)

    case 'diatutkinto':
      if (!opintojenRahoitus || !suorituskieli || maksuton === undefined) {
        return undefined
      }
      return createDIAOpiskeluoikeus(
        suorituksenTyyppi,
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        suorituskieli,
        maksuton
      )

    case 'ibtutkinto':
      if (!opintojenRahoitus || !suorituskieli || maksuton === undefined) {
        return undefined
      }
      return createIBOpiskeluoikeus(
        suorituksenTyyppi,
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        suorituskieli,
        maksuton
      )

    case 'internationalschool':
      if (!opintojenRahoitus || !internationalSchoolGrade || !suorituskieli)
        return undefined
      return createInternationalSchoolOpiskeluoikeus(
        organisaatio,
        alku,
        tila,
        opintojenRahoitus,
        internationalSchoolGrade,
        suorituskieli,
        maksuton
      )

    default:
      console.error(
        'createOpiskeluoikeus does not support',
        opiskeluoikeudenTyyppi.koodiarvo
      )
  }
}
