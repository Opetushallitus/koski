import React, { useCallback, useMemo } from 'react'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { CommonProps, common } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../controls/Select'
import {
  KoodistokoodiviiteKoodistonNimellä,
  useKoodisto
} from '../../appstate/koodisto'

export type KoodistoViewProps<T extends string> = CommonProps<
  FieldViewerProps<
    Koodistokoodiviite<T>,
    {
      koodistoUri: T
      testId: string
    }
  >
>

export const KoodistoView = <T extends string>(props: KoodistoViewProps<T>) => {
  const testId = useTestId(props.testId)
  return (
    <div {...common(props)} data-testid={testId}>
      {t(props.value?.nimi) || '–'}
    </div>
  )
}

export const KoodistoViewSpan = <T extends string>(
  props: KoodistoViewProps<T>
) => {
  const testId = useTestId(props.testId)
  return (
    <span {...common(props)} data-testid={testId}>
      {t(props.value?.nimi) || '–'}
    </span>
  )
}

export type KoodistoEditProps<T extends string> = CommonProps<
  FieldEditorProps<
    Koodistokoodiviite<T>,
    {
      koodistoUri: T
      testId: string
      koodiarvot?: string[]
      zeroValueOption?: boolean
    }
  >
>

export const KoodistoEdit = <T extends string>(props: KoodistoEditProps<T>) => {
  const koodisto = useKoodisto(props.koodistoUri, props.koodiarvot)
  const options = useMemo(() => {
    const koodistoOptions = (koodisto ?? []).map((k) => ({
      key: k.koodiviite.koodiarvo,
      label: t(k.koodiviite.nimi),
      value: k.koodiviite
    }))

    const zeroValue = {
      key: 'Ei valintaa',
      label: t('Ei valintaa'),
      value: undefined
    }

    return props.zeroValueOption
      ? [zeroValue, ...koodistoOptions]
      : koodistoOptions
  }, [koodisto, props.zeroValueOption])

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
