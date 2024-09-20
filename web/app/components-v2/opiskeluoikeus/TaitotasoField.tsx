import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { LukutaitokoulutuksenArviointi } from '../../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { parasArviointi } from '../../util/arvioinnit'
import { koodiviiteId, KoodiviiteWithOptionalUri } from '../../util/koodisto'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps } from '../CommonProps'
import {
  groupKoodistoToOptions,
  OptionList,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

type TaitotasoOf<T extends LukutaitokoulutuksenArviointi> = Exclude<
  T['taitotaso'],
  KoodiviiteWithOptionalUri
>

export type TaitotasoViewProps<T extends LukutaitokoulutuksenArviointi> =
  CommonProps<FieldViewerProps<T[] | undefined, EmptyObject>>

export const TaitotasoView = <T extends LukutaitokoulutuksenArviointi>(
  props: TaitotasoViewProps<T>
) => {
  const arvionti = parasArviointi(props.value || [])
  return arvionti !== undefined ? (
    <TestIdText {...common(props)} id="taitotaso.value">
      {t(arvionti.taitotaso.nimi)}
    </TestIdText>
  ) : null
}

export type TaitotasoEditProps<T extends LukutaitokoulutuksenArviointi> =
  CommonProps<FieldEditorProps<T[] | undefined, EmptyObject>>

export const TaitotasoEdit = <T extends LukutaitokoulutuksenArviointi>(
  props: TaitotasoEditProps<T>
) => {
  const koodisto = useKoodisto('arviointiasteikkokehittyvankielitaidontasot')
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialArviointi =
    props.initialValue && parasArviointi(props.initialValue)
  const initialValue =
    initialArviointi?.taitotaso && koodiviiteId(initialArviointi.taitotaso)
  const arviointi = props.value && parasArviointi(props.value)
  const selectedValue =
    arviointi?.taitotaso && koodiviiteId(arviointi?.taitotaso)
  const paras = parasArviointi(props.value || [])

  const onChange = (option?: SelectOption<TaitotasoOf<T>>) => {
    if (option !== undefined && option.value !== undefined) {
      props.onChange(
        option?.value &&
          updateLukutaitokoulutuksenArvioinnit(
            // @ts-expect-error TODO: Tarkista tyypityksen korjaus
            { ...paras, taitotaso: option.value },
            props.initialValue || []
          )
      )
    }
  }

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto as OptionList<TaitotasoOf<T>>}
        onChange={onChange}
        testId="taitotaso.edit"
      />
    )
  )
}

const updateLukutaitokoulutuksenArvioinnit = <
  T extends LukutaitokoulutuksenArviointi
>(
  arviointi: T,
  arvioinnit: T[]
): T[] =>
  pipe(
    arvioinnit,
    A.init,
    O.fold(
      () => [arviointi],
      (vanhatArvioinnit) => [...vanhatArvioinnit, arviointi]
    )
  )
