import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { LukutaitokoulutuksenArviointi } from '../../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { koodiviiteId, KoodiviiteWithOptionalUri } from '../../util/koodisto'
import { viimeisinLukutaitokoulutuksenArviointi } from '../../util/schema'
import { common, CommonProps, testId } from '../CommonProps'
import {
  groupKoodistoToOptions,
  OptionList,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { VSTKehittyvänKielenTaitotasonArviointi } from '../../types/fi/oph/koski/schema/VSTKehittyvanKielenTaitotasonArviointi'

type TaitotasoOf<T extends VSTKehittyvänKielenTaitotasonArviointi> = Exclude<
  T['taso'],
  KoodiviiteWithOptionalUri
>

export type TaitotasoViewProps<
  T extends VSTKehittyvänKielenTaitotasonArviointi
> = CommonProps<FieldViewerProps<T | undefined, {}>>

export const TaitotasoView = <T extends VSTKehittyvänKielenTaitotasonArviointi>(
  props: TaitotasoViewProps<T>
) => {
  return props.value !== undefined ? (
    <span {...common(props)} {...testId(props)}>
      {t(props.value.taso.nimi)}
    </span>
  ) : null
}

export type TaitotasoEditProps<
  T extends VSTKehittyvänKielenTaitotasonArviointi
> = CommonProps<FieldEditorProps<T | undefined, {}>>

export const TaitotasoEdit = <T extends VSTKehittyvänKielenTaitotasonArviointi>(
  props: TaitotasoEditProps<T>
) => {
  const koodisto = useKoodisto('arviointiasteikkokehittyvankielitaidontasot')
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialTaitotaso = props.initialValue
  const initialValue =
    initialTaitotaso !== undefined
      ? koodiviiteId(initialTaitotaso.taso)
      : undefined
  const taitotaso = props.value && props.value
  const selectedValue =
    taitotaso !== undefined ? koodiviiteId(taitotaso.taso) : undefined

  const onChange = (option?: SelectOption<TaitotasoOf<T>>) => {
    console.log(option?.value)
    props.onChange(
      // TODO: Tarkasta, voiko tyypityksen korjata
      // @ts-expect-error
      VSTKehittyvänKielenTaitotasonArviointi({
        // @ts-expect-error
        taso: option?.value
      })
    )
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
