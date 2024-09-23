import React, { useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { VSTKehittyvänKielenTaitotasonArviointi } from '../../types/fi/oph/koski/schema/VSTKehittyvanKielenTaitotasonArviointi'
import { koodiviiteId, KoodiviiteWithOptionalUri } from '../../util/koodisto'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import {
  groupKoodistoToOptions,
  OptionList,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

type TaitotasoOf<T extends VSTKehittyvänKielenTaitotasonArviointi> = Exclude<
  T['taso'],
  KoodiviiteWithOptionalUri
>

export type KehittyvänKielenTaitotasoViewProps<
  T extends VSTKehittyvänKielenTaitotasonArviointi
> = CommonProps<FieldViewerProps<T | undefined, EmptyObject>>

export const KehittyvänKielenTaitotasoView = <
  T extends VSTKehittyvänKielenTaitotasonArviointi
>(
  props: KehittyvänKielenTaitotasoViewProps<T>
) => {
  return props.value !== undefined ? (
    <TestIdText {...props} id={`${props.testId || 'taitotaso'}.value`}>
      {t(props.value.taso.nimi)}
    </TestIdText>
  ) : null
}

export type KehittyvänKielenTaitotasoEditProps<
  T extends VSTKehittyvänKielenTaitotasonArviointi
> = CommonProps<FieldEditorProps<T | undefined, EmptyObject>>

export const KehittyvänKielenTaitotasoEdit = <
  T extends VSTKehittyvänKielenTaitotasonArviointi
>(
  props: KehittyvänKielenTaitotasoEditProps<T>
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
        testId={`${props.testId || 'taitotaso'}.edit`}
      />
    )
  )
}
