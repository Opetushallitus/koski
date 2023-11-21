import React, { useCallback, useMemo } from 'react'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { CommonProps, common } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TestIdLayer } from '../../appstate/useTestId'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../controls/Select'
import { useKoodisto } from '../../appstate/koodisto'

export type KoodistoViewProps<T extends string> = CommonProps<
  FieldViewerProps<
    Koodistokoodiviite<T>,
    {
      koodistoUri: T
      testId: string
    }
  >
>

export const KoodistoView = <T extends string>(props: KoodistoViewProps<T>) => (
  <div {...common(props)} data-testid={props.testId}>
    {t(props.value?.nimi) || '–'}
  </div>
)

export type KoodistoEditProps<T extends string> = CommonProps<
  FieldEditorProps<
    Koodistokoodiviite<T>,
    {
      koodistoUri: T
      testId: string
    }
  >
>

export const KoodistoEdit = <T extends string>(props: KoodistoEditProps<T>) => {
  const koodisto = useKoodisto(props.koodistoUri)
  const options = useMemo(
    () =>
      (koodisto ?? []).map((k) => ({
        key: k.koodiviite.koodiarvo,
        label: t(k.koodiviite.nimi),
        value: k.koodiviite
      })),
    [koodisto]
  )

  const { onChange } = props
  const update = useCallback(
    (option?: SelectOption<Koodistokoodiviite<T>>) => {
      onChange(option?.value)
    },
    [onChange]
  )

  return (
    <Select
      value={props.value?.koodiarvo}
      options={options}
      onChange={update}
      testId={props.testId}
    />
  )
}
