import React, { useEffect, useMemo } from 'react'
import { isSuccess, useApiWithParams } from '../api-fetch'
import { useChildSchema, useChildSchemaSafe } from '../appstate/constraints'
import { useKoodisto, useKoodistoOfConstraint } from '../appstate/koodisto'
import { Checkbox } from '../components-v2/controls/Checkbox'
import { DateEdit } from '../components-v2/controls/DateField'
import { KieliSelect } from '../components-v2/controls/KieliSelect'
import {
  Select,
  SelectOption,
  koodiviiteToOption
} from '../components-v2/controls/Select'
import { t } from '../i18n/i18n'
import { isJotpaRahoituksenKoodistoviite } from '../jotpa/jotpa'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { OpiskeluoikeusClass } from '../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import { koodistokoodiviiteId } from '../util/koodisto'
import { fetchOrganisaationOpiskeluoikeustyypit } from '../util/koskiApi'
import { DialogMaksuttomuusSelect } from './DialogMaksuttomuusSelect'
import { DialogKoodistoSelect } from './components/DialogKoodistoSelect'
import { OppilaitosSearch } from './components/OppilaitosSearch'
import { OppilaitosSelect, OrgType } from './components/OppilaitosSelect'
import {
  UusiOpiskeluoikeusDialogState,
  useUusiOpiskeluoikeusDialogState
} from './state'
import { SuoritusFields } from './suoritus/SuoritusFields'

export type UusiOpiskeluoikeusFormProps = {
  onResult: (opiskeluoikeus?: Opiskeluoikeus) => void
}

const valittavatOrganisaatiotyypit: OrgType[] = [
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
          {t('Hankintakoulutus')}
          {state.hankintakoulutus.value !== 'tpo' && (
            <Checkbox
              label={t(
                'Esiopetus ostetaan oman organisaation ulkopuolelta ostopalveluna tai palvelusetelinä'
              )}
              checked={state.hankintakoulutus.value === 'esiopetus'}
              onChange={(opt) =>
                state.hankintakoulutus.set(opt ? 'esiopetus' : undefined)
              }
            />
          )}
          {state.hankintakoulutus.value !== 'esiopetus' && (
            <Checkbox
              label={t(
                'Taiteen perusopetus hankintakoulutuksena järjestetään oman organisaation ulkopuolelta'
              )}
              checked={state.hankintakoulutus.value === 'tpo'}
              onChange={(opt) =>
                state.hankintakoulutus.set(opt ? 'tpo' : undefined)
              }
            />
          )}
          {state.varhaiskasvatuksenJärjestämistapa.visible && (
            <>
              {t('Varhaiskasvatuksen järjestämismuoto')}
              <DialogKoodistoSelect
                state={state.varhaiskasvatuksenJärjestämistapa}
                koodistoUri="vardajarjestamismuoto"
                koodiarvot={['JM02', 'JM03']}
                testId="varhaiskasvatuksenJärjestämismuoto"
              />
            </>
          )}

          {t('Oppilaitos')}
          {state.hankintakoulutus.value ? (
            <OppilaitosSearch
              value={state.oppilaitos.value}
              onChange={state.oppilaitos.set}
              orgTypes={valittavatOrganisaatiotyypit}
            />
          ) : (
            <OppilaitosSelect
              value={state.oppilaitos.value}
              onChange={state.oppilaitos.set}
              orgTypes={valittavatOrganisaatiotyypit}
            />
          )}
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

      {state.maksuton.visible && <DialogMaksuttomuusSelect state={state} />}
    </section>
  )
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

const opiskeluoikeudenTilaClass = (
  ooClass?: OpiskeluoikeusClass,
  suoritustyyppi?: string
): string | undefined => {
  switch (ooClass?.className) {
    case VapaanSivistystyönOpiskeluoikeus.className:
      switch (suoritustyyppi) {
        case 'vstoppivelvollisillesuunnattukoulutus':
        case 'vstmaahanmuuttajienkotoutumiskoulutus':
        case 'vstlukutaitokoulutus':
          return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
        case 'vstjotpakoulutus':
          return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
        case 'vstosaamismerkki':
          return VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className
        case 'vstvapaatavoitteinenkoulutus':
          return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className
        default:
          return undefined
      }
    default: {
      const jaksot = ooClass?.opiskeluoikeusjaksot || []
      if (jaksot.length > 1) {
        throw new Error(
          `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden tilan luokka mahdollinen: ${ooClass?.className}: ${jaksot.join(', ')}`
        )
      }
      return jaksot[0]
    }
  }
}

export const opiskeluoikeudenLisätiedotClass = (
  ooClass?: OpiskeluoikeusClass
): string | undefined => {
  const lisätiedot = ooClass?.lisätiedot || []
  if (lisätiedot.length > 1) {
    throw new Error(
      `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden lisätietoluokka mahdollinen: ${lisätiedot.join(', ')}`
    )
  }
  return lisätiedot[0]
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
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
    'tila'
  )

  const koodistot =
    useKoodistoOfConstraint<'koskiopiskeluoikeudentila'>(opiskelujaksonTila)

  const options = useMemo(
    () =>
      koodistot
        ? koodistot
            .flatMap((k) =>
              k.koodiviite.koodiarvo !== 'mitatoity' ? [k.koodiviite] : []
            )
            .map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => {
    const defaults = [
      'koskiopiskeluoikeudentila_lasna',
      'koskiopiskeluoikeudentila_valmistunut', // Opiskeluoikeus halutaan merkitä tavallisesti suoraan valmistuneeksi, jolla sillä ei ole läsnä-tilaa
      'koskiopiskeluoikeudentila_hyvaksytystisuoritettu'
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
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
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
    opiskeluoikeudenLisätiedotClass(className),
    'jotpaAsianumero'
  )

  const koodistot = useKoodistoOfConstraint<'jotpaasianumero'>(
    asianumeroSchema &&
      state.opintojenRahoitus.value &&
      isJotpaRahoituksenKoodistoviite(state.opintojenRahoitus.value)
      ? asianumeroSchema
      : null
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
