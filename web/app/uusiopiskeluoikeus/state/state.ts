import { useCallback, useMemo } from 'react'
import { isSuccess, useApiOnce } from '../../api-fetch'
import { useSchema } from '../../appstate/constraints'
import { Peruste } from '../../appstate/peruste'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { todayISODate } from '../../date/date'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuAmmatillinenKoulutus } from '../../types/fi/oph/koski/schema/MuuAmmatillinenKoulutus'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isSuorituskielellinen } from '../../types/fi/oph/koski/schema/Suorituskielellinen'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus } from '../../types/fi/oph/koski/schema/TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaKoulutus'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { OpiskeluoikeusClass } from '../../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import * as C from '../../util/constraints'
import { fetchOpiskeluoikeusClassMapping } from '../../util/koskiApi'
import { createOpiskeluoikeus } from '../opiskeluoikeusCreator'
import {
  isVieraanKielenOppiaine,
  isÄidinkielenOppiaine
} from '../opiskeluoikeusCreator/yleissivistavat'
import { opiskeluoikeudenLisätiedotClass } from './opiskeluoikeudenLisätiedotClass'
import { SuoritusClass } from '../../types/fi/oph/koski/typemodel/SuoritusClass'
import { PreIBSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuoritus2019'
import { AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus } from '../../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
import { AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTyyppi } from '../opiskeluoikeusCreator/ammatillinenTutkinto'
import { UIPreIb2019PääsuorituksenTyyppi } from '../opiskeluoikeusCreator/ibTutkinto'

export type UusiOpiskeluoikeusDialogState = {
  hankintakoulutus: DialogField<Hankintakoulutus>
  oppilaitos: DialogField<OrganisaatioHierarkia>
  opiskeluoikeus: DialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>
  päätasonSuoritus: DialogField<Koodistokoodiviite<'suorituksentyyppi'>>
  peruste: DialogField<Peruste>
  suorituskieli: DialogField<Koodistokoodiviite<'kieli'>>
  aloituspäivä: DialogField<string>
  tila: DialogField<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>
  maksuton: DialogField<boolean | null>
  opintojenRahoitus: DialogField<Koodistokoodiviite<'opintojenrahoitus'>>
  tuvaJärjestämislupa: DialogField<Koodistokoodiviite<'tuvajarjestamislupa'>>
  jotpaAsianumero: DialogField<Koodistokoodiviite<'jotpaasianumero'>>
  opintokokonaisuus: DialogField<Koodistokoodiviite<'opintokokonaisuudet'>>
  tpoOppimäärä: DialogField<Koodistokoodiviite<'taiteenperusopetusoppimaara'>>
  tpoTaiteenala: DialogField<Koodistokoodiviite<'taiteenperusopetustaiteenala'>>
  tpoToteutustapa: DialogField<
    Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
  >
  varhaiskasvatuksenJärjestämistapa: DialogField<
    Koodistokoodiviite<'vardajarjestamismuoto'>
  >
  osaamismerkki: DialogField<Koodistokoodiviite<'osaamismerkit'>>
  tutkinto: DialogField<TutkintoPeruste>
  suoritustapa: DialogField<
    Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>
  >
  muuAmmatillinenKoulutus: DialogField<MuuAmmatillinenKoulutus>
  tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus: DialogField<TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus>
  curriculum: DialogField<
    Koodistokoodiviite<'europeanschoolofhelsinkicurriculum'>
  >
  internationalSchoolGrade: DialogField<
    Koodistokoodiviite<'internationalschoolluokkaaste'>
  >
  oppiaine: DialogField<Koodistokoodiviite<'koskioppiaineetyleissivistava'>>
  kieliaineenKieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  äidinkielenKieli: DialogField<
    Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  >
  ooMapping?: OpiskeluoikeusClass[]
  result?: Opiskeluoikeus
}

export type Hankintakoulutus = 'esiopetus' | 'tpo' | undefined
export const useUusiOpiskeluoikeusDialogState =
  (): UusiOpiskeluoikeusDialogState => {
    const ooMappingCall = useApiOnce(fetchOpiskeluoikeusClassMapping)
    const ooMapping = isSuccess(ooMappingCall) ? ooMappingCall.data : undefined
    const suoritusMapping = useMemo(
      () =>
        hackSuoritusMappingKunSamatSuorituksenTyypit(
          (ooMapping || []).flatMap((oo) => oo.suoritukset)
        ),
      [ooMapping]
    )

    // Oppilaitos
    const hankintakoulutus = useDialogField<Hankintakoulutus>(true)
    const oppilaitos = useDialogField<OrganisaatioHierarkia>(true)
    const oppilaitosValittu = oppilaitos.value !== undefined

    // Opiskeluoikeus
    const opiskeluoikeus =
      useDialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>(
        oppilaitosValittu
      )
    const opiskeluoikeusValittu = opiskeluoikeus.value !== undefined
    const opiskeluoikeudeksiValittu = useCallback(
      (...tyypit: string[]): boolean =>
        !!opiskeluoikeus.value &&
        tyypit.includes(opiskeluoikeus.value.koodiarvo),
      [opiskeluoikeus.value]
    )
    const opiskeluoikeusClass = opiskeluoikeustyyppiToClassNames(
      ooMapping,
      opiskeluoikeus.value?.koodiarvo
    )

    // Päätason suoritus
    const päätasonSuoritus = useDialogField<
      Koodistokoodiviite<'suorituksentyyppi'>
    >(opiskeluoikeusValittu)
    const päätasonSuoritusClass = suoritusMapping.find(
      (s) => s.tyyppi === päätasonSuoritus.value?.koodiarvo
    )?.className
    const päätasonSuoritusValittu = päätasonSuoritus.value !== undefined
    const päätasonSuoritukseksiValittu = useCallback(
      (...tyypit: string[]): boolean =>
        !!päätasonSuoritus.value &&
        tyypit.includes(päätasonSuoritus.value.koodiarvo),
      [päätasonSuoritus.value]
    )

    // Peruste
    const peruste = useDialogField<Peruste>(
      opiskeluoikeudeksiValittu(
        'perusopetus',
        'perusopetukseenvalmistavaopetus',
        'perusopetuksenlisaopetus',
        'aikuistenperusopetus',
        'esiopetus',
        'tuva',
        'taiteenperusopetus',
        'luva',
        'lukiokoulutus'
      ) ||
        päätasonSuoritukseksiValittu(
          'telma',
          'valma',
          'vstoppivelvollisillesuunnattukoulutus',
          'vstmaahanmuuttajienkotoutumiskoulutus',
          'vstlukutaitokoulutus'
        )
    )

    // Suorituskieli
    const suorituskieli = useDialogField<Koodistokoodiviite<'kieli'>>(
      isSuorituskielellinen(asObject(päätasonSuoritusClass))
    )

    // Aloituspäivä
    const aloituspäivä = useDialogField<string>(
      opiskeluoikeusValittu,
      todayISODate
    )

    // Opiskeluoikeuden tila
    const tila = useDialogField<
      Koodistokoodiviite<'koskiopiskeluoikeudentila'>
    >(opiskeluoikeusValittu)

    // Opintojen rahoitus
    const opintojenRahoitus = useDialogField<
      Koodistokoodiviite<'opintojenrahoitus'>
    >(
      päätasonSuoritusValittu &&
        opiskeluoikeus.value?.koodiarvo !== 'europeanschoolofhelsinki'
    )

    // Tuva-järjestämislupa
    const tuvaJärjestämislupa =
      useDialogField<Koodistokoodiviite<'tuvajarjestamislupa'>>(true)

    const opiskeluoikeudenLisätiedot = useSchema(
      opiskeluoikeudenLisätiedotClass(
        opiskeluoikeusClass,
        tuvaJärjestämislupa.value
      )
    )

    // Opintokokonaisuus (vst jotpa, vst vapaatavoitteinen, sekä muu kuin säännelty koulutus)
    const opintokokonaisuus =
      useDialogField<Koodistokoodiviite<'opintokokonaisuudet'>>(true)

    // Jotpa-asianumerollinen
    const jotpaAsianumero = useDialogField<
      Koodistokoodiviite<'jotpaasianumero'>
    >(C.hasProp(opiskeluoikeudenLisätiedot, 'jotpaAsianumero'))

    // Taiteen perusopetuksen oppimäärä, taiteenala ja koulutuksen toteutustapa
    const tpoOppimäärä =
      useDialogField<Koodistokoodiviite<'taiteenperusopetusoppimaara'>>(true)
    const tpoTaiteenala =
      useDialogField<Koodistokoodiviite<'taiteenperusopetustaiteenala'>>(true)
    const tpoToteutustapa =
      useDialogField<
        Koodistokoodiviite<'taiteenperusopetuskoulutuksentoteutustapa'>
      >(true)

    // Varhaiskasvatuksen järjestämistapa
    const varhaiskasvatuksenJärjestämistapa = useDialogField<
      Koodistokoodiviite<'vardajarjestamismuoto'>
    >(hankintakoulutus.value === 'esiopetus')

    // Vapaan sivistystyön koulutuksen osaamismerkki
    const osaamismerkki =
      useDialogField<Koodistokoodiviite<'osaamismerkit'>>(true)

    // Ammatillisen koulutuksen tutkinto
    !!päätasonSuoritus.value &&
      ['ammatillinentutkinto', 'ammatillinentutkintoosittainen'].includes(
        päätasonSuoritus.value?.koodiarvo
      )

    const tutkinto = useDialogField<TutkintoPeruste>(
      päätasonSuoritukseksiValittu(
        'ammatillinentutkinto',
        'ammatillinentutkintoosittainen',
        'nayttotutkintoonvalmistavakoulutus'
      )
    )

    const suoritustapa = useDialogField<
      Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>
    >(
      päätasonSuoritukseksiValittu(
        'ammatillinentutkinto',
        'ammatillinentutkintoosittainen'
      )
    )

    const muuAmmatillinenKoulutus = useDialogField<MuuAmmatillinenKoulutus>(
      päätasonSuoritukseksiValittu('muuammatillinenkoulutus')
    )

    const tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =
      useDialogField<TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus>(
        päätasonSuoritukseksiValittu(
          'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
        )
      )

    // European School of Helsinki
    const curriculum = useDialogField<
      Koodistokoodiviite<'europeanschoolofhelsinkicurriculum'>
    >(opiskeluoikeudeksiValittu('europeanschoolofhelsinki'))

    // International School
    const internationalSchoolGrade = useDialogField<
      Koodistokoodiviite<'internationalschoolluokkaaste'>
    >(opiskeluoikeudeksiValittu('internationalschool'))

    // Perusopetuksen ja lukion oppiaineen koulutus
    const oppiaine = useDialogField<
      Koodistokoodiviite<'koskioppiaineetyleissivistava'>
    >(
      päätasonSuoritukseksiValittu(
        'nuortenperusopetuksenoppiaineenoppimaara',
        'perusopetuksenoppiaineenoppimaara',
        'lukionoppiaineenoppimaara',
        'luvalukionoppiaine',
        'preiboppiaine'
      )
    )

    const kieliaineenKieli = useDialogField<
      Koodistokoodiviite<'kielivalikoima'>
    >(isVieraanKielenOppiaine(oppiaine.value?.koodiarvo))

    const äidinkielenKieli = useDialogField<
      Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
    >(isÄidinkielenOppiaine(oppiaine.value?.koodiarvo))

    // Opiskelun maksuttomuus
    const maksuttomuustiedollinen = useMemo(() => {
      // Tarkista ensin onko koko maksuttomuustietoa edes valitun opiskeluoikeuden tietomallissa
      if (!C.hasProp(opiskeluoikeudenLisätiedot, 'maksuttomuus')) return false

      // Osalle vst-opiskeluoikeuksista maksuttomuustietoa ei käytetä päätason suorituksen perusteella
      if (
        päätasonSuoritukseksiValittu(
          'vstjotpakoulutus',
          'vstosaamismerkki',
          'vstvapaatavoitteinenkoulutus'
        )
      ) {
        return false
      }

      // European School of Helsingille maksuttomuustieto on virheellisesti tietomallissa
      if (opiskeluoikeudeksiValittu('europeanschoolofhelsinki')) return false

      // International School of Helsingille maksuttomuustieto valitaan vain diploma-tason vuosiluokille
      if (opiskeluoikeudeksiValittu('internationalschool')) {
        return (
          !!internationalSchoolGrade?.value &&
          ['11', '12'].includes(internationalSchoolGrade?.value?.koodiarvo)
        )
      }

      return true
    }, [
      internationalSchoolGrade?.value,
      opiskeluoikeudeksiValittu,
      opiskeluoikeudenLisätiedot,
      päätasonSuoritukseksiValittu
    ])

    const maksuton = useDialogField<boolean | null>(
      maksuttomuustiedollinen,
      () => null
    )

    // Validi opiskeluoikeus
    const result = useMemo(
      () =>
        oppilaitos.value &&
        opiskeluoikeus.value &&
        päätasonSuoritus.value &&
        aloituspäivä.value &&
        tila.value
          ? createOpiskeluoikeus(
              oppilaitos.value,
              opiskeluoikeus.value,
              päätasonSuoritus.value,
              peruste.value,
              aloituspäivä.value,
              tila.value,
              suorituskieli.value,
              maksuton.value,
              opintojenRahoitus.value,
              tuvaJärjestämislupa.value,
              opintokokonaisuus.value,
              jotpaAsianumero.value,
              tpoOppimäärä.value,
              tpoTaiteenala.value,
              tpoToteutustapa.value,
              varhaiskasvatuksenJärjestämistapa.value,
              hankintakoulutus.value,
              osaamismerkki.value,
              tutkinto.value,
              suoritustapa.value,
              muuAmmatillinenKoulutus.value,
              tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus.value,
              curriculum.value,
              internationalSchoolGrade.value,
              oppiaine.value,
              kieliaineenKieli.value,
              äidinkielenKieli.value
            )
          : undefined,
      [
        aloituspäivä.value,
        curriculum.value,
        hankintakoulutus.value,
        internationalSchoolGrade.value,
        jotpaAsianumero.value,
        kieliaineenKieli.value,
        maksuton.value,
        muuAmmatillinenKoulutus.value,
        opintojenRahoitus.value,
        opintokokonaisuus.value,
        opiskeluoikeus.value,
        oppiaine.value,
        oppilaitos.value,
        osaamismerkki.value,
        peruste.value,
        päätasonSuoritus.value,
        suorituskieli.value,
        suoritustapa.value,
        tila.value,
        tpoOppimäärä.value,
        tpoTaiteenala.value,
        tpoToteutustapa.value,
        tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus.value,
        tutkinto.value,
        tuvaJärjestämislupa.value,
        varhaiskasvatuksenJärjestämistapa.value,
        äidinkielenKieli.value
      ]
    )

    return {
      hankintakoulutus,
      oppilaitos,
      opiskeluoikeus,
      päätasonSuoritus,
      peruste,
      suorituskieli,
      aloituspäivä,
      tila,
      maksuton,
      opintojenRahoitus,
      tuvaJärjestämislupa,
      jotpaAsianumero,
      opintokokonaisuus,
      tpoOppimäärä,
      tpoTaiteenala,
      tpoToteutustapa,
      varhaiskasvatuksenJärjestämistapa,
      osaamismerkki,
      tutkinto,
      suoritustapa,
      muuAmmatillinenKoulutus,
      tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
      curriculum,
      internationalSchoolGrade,
      oppiaine,
      kieliaineenKieli,
      äidinkielenKieli,
      ooMapping,
      result
    }
  }

export const asObject = (className?: string) =>
  className ? { $class: className } : undefined

export const opiskeluoikeustyyppiToClassNames = (
  ooMapping?: OpiskeluoikeusClass[],
  tyyppi?: string
): OpiskeluoikeusClass | undefined => {
  return tyyppi !== undefined && ooMapping
    ? ooMapping.find((c) => c.tyyppi === tyyppi)
    : undefined
}

// Hack, joka tarvitaan koska joillain uusilla suorituksilla on samat suorituksen tyypit (Pre-IB ja ammatillisen osittainen suoritus useasta tutkinnosta).
// Käyttöliittymässä leikitään että uudemman suorituksen tyyppi olisi 'preiboppimaara2019' tai 'ammatillinentutkintoosittainenuseastatutkinnosta',
// mutta se mäpätään takaisin oikeaksi opiskeluoikeutta luotaessa.
const hackSuoritusMappingKunSamatSuorituksenTyypit = (
  cs: SuoritusClass[]
): SuoritusClass[] =>
  cs.map((cn) => {
    if (cn.className === PreIBSuoritus2019.className) {
      return { ...cn, tyyppi: UIPreIb2019PääsuorituksenTyyppi }
    } else if (
      cn.className ===
      AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus.className
    ) {
      return {
        ...cn,
        tyyppi: AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTyyppi
      }
    } else return cn
  })
