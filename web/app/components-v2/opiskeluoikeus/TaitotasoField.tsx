import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { LukutaitokoulutuksenArviointi } from '../../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { koodiviiteId, KoodiviiteWithOptionalUri } from '../../util/koodisto'
import {
  viimeisinArviointi,
  viimeisinLukutaitokoulutuksenArviointi
} from '../../util/schema'
import { common, CommonProps, testId } from '../CommonProps'
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
  CommonProps<FieldViewerProps<T[] | undefined, {}>>

export const TaitotasoView = <T extends LukutaitokoulutuksenArviointi>(
  props: TaitotasoViewProps<T>
) => {
  const arviointi =
    props.value !== undefined &&
    viimeisinLukutaitokoulutuksenArviointi(props.value)
  console.log('arviointi', arviointi)
  return arviointi ? (
    <span {...common(props)} {...testId(props)}>
      {t(arviointi.taitotaso.nimi)}
    </span>
  ) : null
}

export type TaitotasoEditProps<T extends LukutaitokoulutuksenArviointi> =
  CommonProps<FieldEditorProps<T[] | undefined, {}>>

export const TaitotasoEdit = <T extends LukutaitokoulutuksenArviointi>(
  props: TaitotasoEditProps<T>
) => {
  const koodisto = useKoodisto('arviointiasteikkokehittyvankielitaidontasot')
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialArviointi =
    props.initialValue &&
    viimeisinLukutaitokoulutuksenArviointi(props.initialValue)
  const initialValue =
    initialArviointi?.taitotaso && koodiviiteId(initialArviointi.taitotaso)
  const arviointi =
    props.value && viimeisinLukutaitokoulutuksenArviointi(props.value)
  const selectedValue =
    arviointi?.taitotaso && koodiviiteId(arviointi?.taitotaso)

  const onChange = (option?: SelectOption<TaitotasoOf<T>>) => {
    // TODO: Päivitä taitotaso viimeisimmästä arvioinnista
    console.log('option', option)
    /*
    props.onChange(
      option?.value &&
        updateLukutaitokoulutuksenArvioinnit(
          props.createTaitotaso(option.value),
          props.initialValue || []
        )
    )
    */
  }

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto as OptionList<TaitotasoOf<T>>}
        onChange={onChange}
        testId={props.testId}
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