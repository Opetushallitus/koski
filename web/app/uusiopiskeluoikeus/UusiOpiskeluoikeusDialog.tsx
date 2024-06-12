import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { isSuccess, useApiOnce, useApiWithParams } from '../api-fetch'
import { useChildSchema } from '../appstate/constraints'
import { useKoodisto, useKoodistoOfConstraint } from '../appstate/koodisto'
import { Peruste } from '../appstate/peruste'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { DateEdit } from '../components-v2/controls/DateField'
import { KieliSelect } from '../components-v2/controls/KieliSelect'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import {
  Select,
  SelectOption,
  koodiviiteToOption
} from '../components-v2/controls/Select'
import { todayISODate } from '../date/date'
import { t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OidOrganisaatio } from '../types/fi/oph/koski/schema/OidOrganisaatio'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isSuorituskielellinen } from '../types/fi/oph/koski/schema/Suorituskielellinen'
import { OpiskeluoikeusClass } from '../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import { koodistokoodiviiteId } from '../util/koodisto'
import {
  fetchOpiskeluoikeusClassMapping,
  fetchOrganisaationOpiskeluoikeustyypit
} from '../util/koskiApi'
import { ClassOf } from '../util/types'
import { OppilaitosSelect, OrgType } from './OppilaitosSelect'
import { createOpiskeluoikeus } from './opiskeluoikeusBuilder'
import { OppimääräFields } from './oppimaara/OppimaaraFields'
import { FlatButton } from '../components-v2/controls/FlatButton'

export type UusiOpiskeluoikeusDialogProps = {
  onSubmit: (opiskeluoikeus: Opiskeluoikeus) => void
  onClose: () => void
}

const organisaatiotyypit: OrgType[] = [
  'OPPILAITOS',
  'OPPISOPIMUSTOIMIPISTE',
  'VARHAISKASVATUKSEN_TOIMIPAIKKA'
]

export const UusiOpiskeluoikeusDialog = (
  props: UusiOpiskeluoikeusDialogProps
) => {
  const state = useUusiOpiskeluoikeusDialogState()
  const opiskeluoikeustyypit = useOpiskeluoikeustyypit(state.oppilaitos.value)
  const tilat = useOpiskeluoikeudenTilat(state)

  const onSubmit = useCallback(() => {
    state.result && props.onSubmit(state.result)
  }, [props, state.result])

  return (
    <Modal>
      <ModalTitle>{t('Opiskeluoikeuden lisäys')}</ModalTitle>
      <ModalBody>
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

        {state.päätasonSuoritus.visible && <OppimääräFields state={state} />}

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
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose}>{t('Peruuta')}</FlatButton>
        <RaisedButton onClick={onSubmit} disabled={!state.result}>
          {t('Lisää opiskeluoikeus')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

export type UusiOpiskeluoikeusDialogState = {
  oppilaitos: DialogField<OidOrganisaatio>
  opiskeluoikeus: DialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>
  päätasonSuoritus: DialogField<Koodistokoodiviite<'suorituksentyyppi'>>
  peruste: DialogField<Peruste>
  suorituskieli: DialogField<Koodistokoodiviite<'kieli'>>
  aloituspäivä: DialogField<string>
  tila: DialogField<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>
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
  const oppilaitos = useDialogField<OidOrganisaatio>(true)
  const oppilaitosValittu = oppilaitos.value !== undefined

  // Opiskeluoikeus
  const opiskeluoikeus =
    useDialogField<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>(
      oppilaitosValittu
    )
  const opiskeluoikeusValittu = opiskeluoikeus.value !== undefined

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

  // Validi opiskeluoikeus
  const result =
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
          suorituskieli.value
        )
      : undefined

  return {
    oppilaitos,
    opiskeluoikeus,
    päätasonSuoritus,
    peruste,
    suorituskieli,
    aloituspäivä,
    tila,
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
  organisaatio?: OidOrganisaatio
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
  const className = opiskeluoikeustyyppiToClassName(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchema(
    className,
    'tila.opiskeluoikeusjaksot.[].tila'
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

const asObject = (className?: string) =>
  className ? { $class: className } : undefined

const opiskeluoikeustyyppiToClassName = (
  ooMapping?: OpiskeluoikeusClass[],
  tyyppi?: string
): ClassOf<Opiskeluoikeus> | undefined => {
  return tyyppi !== undefined && ooMapping
    ? (ooMapping.find((c) => c.tyyppi === tyyppi)
        ?.className as ClassOf<Opiskeluoikeus>)
    : undefined
}
