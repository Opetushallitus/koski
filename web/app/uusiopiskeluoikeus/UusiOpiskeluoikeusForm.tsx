import React, { useEffect, useMemo, useState } from 'react'
import { isSuccess, useApiOnce, useApiWithParams } from '../api-fetch'
import {
  useChildSchema,
  useChildSchemaSafe,
  useSchema
} from '../appstate/constraints'
import { useKoodisto, useKoodistoOfConstraint } from '../appstate/koodisto'
import { Peruste } from '../appstate/peruste'
import { DateEdit } from '../components-v2/controls/DateField'
import { KieliSelect } from '../components-v2/controls/KieliSelect'
import {
  RadioButtonOption,
  RadioButtons
} from '../components-v2/controls/RadioButtons'
import {
  Select,
  SelectOption,
  koodiviiteToOption
} from '../components-v2/controls/Select'
import { todayISODate } from '../date/date'
import { t } from '../i18n/i18n'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isSuorituskielellinen } from '../types/fi/oph/koski/schema/Suorituskielellinen'
import { OpiskeluoikeusClass } from '../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import * as C from '../util/constraints'
import { koodistokoodiviiteId } from '../util/koodisto'
import {
  fetchOpiskeluoikeusClassMapping,
  fetchOrganisaationOpiskeluoikeustyypit
} from '../util/koskiApi'
import { OppilaitosSelect, OrgType } from './OppilaitosSelect'
import { createOpiskeluoikeus } from './opiskeluoikeusBuilder'
import { SuoritusFields } from './suoritus/SuoritusFields'

export type UusiOpiskeluoikeusFormProps = {
  onResult: (opiskeluoikeus?: Opiskeluoikeus) => void
}

const organisaatiotyypit: OrgType[] = [
  'OPPILAITOS',
  'OPPISOPIMUSTOIMIPISTE',
  'VARHAISKASVATUKSEN_TOIMIPAIKKA'
]

export const UusiOpiskeluoikeusForm = (props: UusiOpiskeluoikeusFormProps) => {
  const state = useUusiOpiskeluoikeusDialogState()
  const opiskeluoikeustyypit = useOpiskeluoikeustyypit(state.oppilaitos.value)
  const tilat = useOpiskeluoikeudenTilat(state)
  const opintojenRahoitukset = useOpintojenRahoitus(state)
  const jotpaAsianumerot = useJotpaAsianumero(state)

  useEffect(() => props.onResult(state.result), [props, state.result])

  return (
    <section>
      {state.oppilaitos.visible && (
        <>
          {t('Oppilaitos')}
          <div>
            <OppilaitosSelect
              value={state.oppilaitos.value}
              onChange={state.oppilaitos.set}
              orgTypes={organisaatiotyypit}
            />
          </div>
        </>
      )}

      {state.opiskeluoikeus.visible && (
        <>
          {t('Opiskeluoikeus')}
          <Select
            options={opiskeluoikeustyypit}
            initialValue={opiskeluoikeustyypit[0]?.key}
            value={
              state.opiskeluoikeus.value &&
              koodistokoodiviiteId(state.opiskeluoikeus.value)
            }
            onChange={(opt) => state.opiskeluoikeus.set(opt?.value)}
            testId="opiskeluoikeus"
          />
        </>
      )}

      {state.päätasonSuoritus.visible && <SuoritusFields state={state} />}

      {state.suorituskieli.visible && (
        <>
          {t('Suorituskieli')}
          <KieliSelect
            initialValue="kieli_FI"
            value={
              state.suorituskieli.value &&
              koodistokoodiviiteId(state.suorituskieli.value)
            }
            onChange={(opt) => state.suorituskieli.set(opt?.value)}
            testId="suorituskieli"
          />
        </>
      )}

      {state.aloituspäivä.visible && (
        <>
          {'Aloituspäivä'}
          <DateEdit
            value={state.aloituspäivä.value}
            onChange={state.aloituspäivä.set}
          />
        </>
      )}

      {state.tila.visible && (
        <>
          {t('Opiskeluoikeuden tila')}
          <Select
            options={tilat.options}
            initialValue={tilat.initialValue}
            value={state.tila.value && koodistokoodiviiteId(state.tila.value)}
            onChange={(opt) => state.tila.set(opt?.value)}
            testId="tila"
          />
        </>
      )}

      {opintojenRahoitukset.options.length > 0 && (
        <>
          {t('Opintojen rahoitus')}
          <Select
            options={opintojenRahoitukset.options}
            initialValue={opintojenRahoitukset.initialValue}
            value={
              state.opintojenRahoitus.value &&
              koodistokoodiviiteId(state.opintojenRahoitus.value)
            }
            onChange={(opt) => state.opintojenRahoitus.set(opt?.value)}
            testId="opintojenRahoitus"
          />
        </>
      )}

      {jotpaAsianumerot.options.length > 0 && (
        <>
          {t('JOTPA asianumero')}
          <Select
            options={jotpaAsianumerot.options}
            initialValue={jotpaAsianumerot.initialValue}
            value={
              state.jotpaAsianumero.value &&
              koodistokoodiviiteId(state.jotpaAsianumero.value)
            }
            onChange={(opt) => state.jotpaAsianumero.set(opt?.value)}
            testId="jotpaAsianumero"
          />
        </>
      )}

      {state.maksuton.visible && (
        <>
          <RadioButtons
            options={maksuttomuusOptions}
            value={maksuttomuusKey(state.maksuton.value)}
            onChange={state.maksuton.set}
            testId="maksuton"
          />
        </>
      )}
    </section>
  )
}

const maksuttomuusKey = (value?: boolean | null): string | undefined =>
  maksuttomuusOptions.find((o) => o.value === value)?.key

const maksuttomuusOptions: Array<RadioButtonOption<boolean | null>> = [
  {
    key: 'eiOvlPiirissä',
    value: null,
    label: t(
      'Henkilö on syntynyt ennen vuotta 2004 ja ei ole laajennetun oppivelvollisuuden piirissä'
    )
  },
  {
    key: 'maksuton',
    value: true,
    label: t('Koulutus on maksutonta')
  },
  {
    key: 'maksullinen',
    value: false,
    label: t('Koulutus on maksullista')
  }
]

export type UusiOpiskeluoikeusDialogState = {
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
  ooMapping?: OpiskeluoikeusClass[]
  result?: Opiskeluoikeus
}

const useUusiOpiskeluoikeusDialogState = (): UusiOpiskeluoikeusDialogState => {
  const ooMappingCall = useApiOnce(fetchOpiskeluoikeusClassMapping)
  const ooMapping = isSuccess(ooMappingCall) ? ooMappingCall.data : undefined
  const suoritusMapping = useMemo(
    () => (ooMapping || []).flatMap((oo) => oo.suoritukset),
    [ooMapping]
  )

  // Oppilaitos
  const oppilaitos = useDialogField<OrganisaatioHierarkia>(true)
  const oppilaitosValittu = oppilaitos.value !== undefined

  // Opiskeluoikeus
  const opiskeluoikeus =
    useDialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>(
      oppilaitosValittu
    )
  const opiskeluoikeusValittu = opiskeluoikeus.value !== undefined
  const opiskeluoikeusClass = opiskeluoikeustyyppiToClassNames(
    ooMapping,
    opiskeluoikeus.value?.koodiarvo
  )
  const opiskeluoikeudenLisätiedot = useSchema(opiskeluoikeusClass?.lisätiedot)

  // Päätason suoritus
  const päätasonSuoritus = useDialogField<
    Koodistokoodiviite<'suorituksentyyppi'>
  >(opiskeluoikeusValittu)
  const päätasonSuoritusClass = suoritusMapping.find(
    (s) => s.tyyppi === päätasonSuoritus.value?.koodiarvo
  )?.className
  const päätasonSuoritusValittu = päätasonSuoritus.value !== undefined

  // Peruste
  const peruste = useDialogField<Peruste>(päätasonSuoritusValittu)

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
  const tila = useDialogField<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>(
    opiskeluoikeusValittu
  )

  // Opiskelun maksuttomuus
  const maksuttomuustiedollinen = C.hasProp(
    opiskeluoikeudenLisätiedot,
    'maksuttomuus'
  )
  const maksuton = useDialogField<boolean | null>(maksuttomuustiedollinen)

  // Opintojen rahoitus
  const opintojenRahoitus = useDialogField<
    Koodistokoodiviite<'opintojenrahoitus'>
  >(päätasonSuoritusValittu)

  // Tuva-järjestämislupa
  const tuvaJärjestämislupa =
    useDialogField<Koodistokoodiviite<'tuvajarjestamislupa'>>(true)

  // Opintokokonaisuus (muu kuin säännelty koulutus)
  const opintokokonaisuus =
    useDialogField<Koodistokoodiviite<'opintokokonaisuudet'>>(true)

  // Jotpa-asianumerollinen
  const jotpaAsianumero = useDialogField<Koodistokoodiviite<'jotpaasianumero'>>(
    C.hasProp(opiskeluoikeudenLisätiedot, 'jotpaAsianumero')
  )

  // Validi opiskeluoikeus
  const result = useMemo(
    () =>
      oppilaitos.value &&
      opiskeluoikeus.value &&
      päätasonSuoritus.value &&
      aloituspäivä.value &&
      tila.value &&
      suorituskieli.value
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
            jotpaAsianumero.value
          )
        : undefined,
    [
      aloituspäivä.value,
      jotpaAsianumero.value,
      maksuton.value,
      opintojenRahoitus.value,
      opintokokonaisuus.value,
      opiskeluoikeus.value,
      oppilaitos.value,
      peruste.value,
      päätasonSuoritus.value,
      suorituskieli.value,
      tila.value,
      tuvaJärjestämislupa.value
    ]
  )

  return {
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
    ooMapping,
    result
  }
}

type DialogField<T> = {
  value?: T
  set: (t?: T) => void
  visible: boolean
}

const useDialogField = <T,>(
  isVisible: boolean,
  defaultValue?: () => T
): DialogField<T> => {
  const [value, set] = useState<T | undefined>(defaultValue)
  const [visible, setVisible] = useState<boolean>(false)

  useEffect(() => {
    setVisible(isVisible)
    if (!isVisible) {
      set(defaultValue)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isVisible])

  return { value, set, visible }
}

const useOpiskeluoikeustyypit = (
  organisaatio?: OrganisaatioHierarkia
): Array<SelectOption<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>> => {
  const apiCall = useApiWithParams(
    fetchOrganisaationOpiskeluoikeustyypit<'opiskeluoikeudentyyppi'>,
    organisaatio?.oid !== undefined ? [organisaatio?.oid] : undefined
  )
  const options = useMemo(
    () => (isSuccess(apiCall) ? apiCall.data : []).map(koodiviiteToOption),
    [apiCall]
  )
  return options
}

export const usePäätasonSuoritustyypit = (
  state: UusiOpiskeluoikeusDialogState
): Array<SelectOption<Koodistokoodiviite<'suorituksentyyppi'>>> => {
  const koodisto = useKoodisto('suorituksentyyppi')?.map((k) => k.koodiviite)
  const ooTyyppi = state.opiskeluoikeus.value
  const ooMapping = state.ooMapping

  return useMemo(() => {
    const ooClassInfo =
      ooTyyppi !== undefined && ooMapping
        ? ooMapping.find((c) => c.tyyppi === ooTyyppi.koodiarvo)
        : undefined
    const koodit = ooClassInfo
      ? ooClassInfo.suoritukset.flatMap((s) => {
          const viite = koodisto?.find((k) => k.koodiarvo === s.tyyppi)
          return viite ? [viite] : []
        })
      : []
    return koodit.map(koodiviiteToOption)
  }, [koodisto, ooMapping, ooTyyppi])
}

const useOpiskeluoikeudenTilat = (
  state: UusiOpiskeluoikeusDialogState
): {
  options: Array<SelectOption<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>>
  initialValue?: string
} => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchema(
    className?.opiskeluoikeusjaksot,
    'tila'
  )

  const koodistot =
    useKoodistoOfConstraint<'koskiopiskeluoikeudentila'>(opiskelujaksonTila)

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => {
    const defaults = [
      'koskiopiskeluoikeudentila_lasna',
      'koskiopiskeluoikeudentila_valmistunut' // Opiskeluoikeus halutaan merkitä tavallisesti suoraan valmistuneeksi, jolla sillä ei ole läsnä-tilaa
    ]
    return defaults.find((tila) => options.find((tt) => tt.key === tila))
  }, [options])

  return { options, initialValue }
}

const useOpintojenRahoitus = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchemaSafe(
    className?.opiskeluoikeusjaksot,
    'opintojenRahoitus'
  )

  const koodistot = useKoodistoOfConstraint<'opintojenrahoitus'>(
    opiskelujaksonTila ? opiskelujaksonTila : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => options[0]?.value?.koodiarvo, [options])

  return { options, initialValue }
}

const useJotpaAsianumero = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const asianumeroSchema = useChildSchemaSafe(
    className?.lisätiedot,
    'jotpaAsianumero'
  )

  const koodistot = useKoodistoOfConstraint<'jotpaasianumero'>(
    asianumeroSchema ? asianumeroSchema : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => options[0]?.value?.koodiarvo, [options])

  return { options, initialValue }
}

const asObject = (className?: string) =>
  className ? { $class: className } : undefined

const opiskeluoikeustyyppiToClassNames = (
  ooMapping?: OpiskeluoikeusClass[],
  tyyppi?: string
): OpiskeluoikeusClass | undefined => {
  return tyyppi !== undefined && ooMapping
    ? ooMapping.find((c) => c.tyyppi === tyyppi)
    : undefined
}
