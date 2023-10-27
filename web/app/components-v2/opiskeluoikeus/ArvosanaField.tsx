import React, { useMemo } from 'react'
import { useSchema } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { parasArviointi, parasArviointiIndex } from '../../util/arvioinnit'
import * as C from '../../util/constraints'
import { KoodiviiteWithOptionalUri, koodiviiteId } from '../../util/koodisto'
import { schemaClassName } from '../../util/types'
import { CommonProps, common, testId } from '../CommonProps'
import {
  OptionList,
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

type ArvosanaOf<T extends Arviointi> = T['arvosana'] //Exclude<
//   T['arvosana'],
//   KoodiviiteWithOptionalUri
// >

export type ArvosanaViewProps<T extends Arviointi> = CommonProps<
  FieldViewerProps<T | undefined, {}>
>

export const ArvosanaView = <T extends Arviointi>(
  props: ArvosanaViewProps<T>
) => {
  return props.value ? (
    <span {...common(props)} {...testId(props)}>
      {t(props.value.arvosana?.nimi)}
    </span>
  ) : null
}

export type ParasArvosanaViewProps<T extends Arviointi> = CommonProps<
  FieldViewerProps<T[] | undefined, {}>
>

export const ParasArvosanaView = <T extends Arviointi>(
  props: ParasArvosanaViewProps<T>
) => {
  const paras = props.value !== undefined && parasArviointi(props.value)
  return paras ? <ArvosanaView {...props} value={paras} /> : null
}

export type ArvosanaEditProps<T extends Arviointi> = CommonProps<
  FieldEditorProps<T | undefined, {}>
> & {
  createArviointi: (arvosana: ArvosanaOf<T>) => T
  disabled?: boolean
}

export const ArvosanaEdit = <T extends Arviointi>(
  props: ArvosanaEditProps<T>
) => {
  const { createArviointi } = props
  const schemaClass = useMemo(
    // @ts-ignore - koska value ja initialValue voivat olla tyhjiä, saadaan $class varmuudella selvitettyä syöttämällä createArviointi-callbackille tyhjä arvosana
    () => schemaClassName(createArviointi(null).$class),
    [createArviointi]
  )
  const arviointiSchema = useSchema(schemaClass)
  const koodisto = useKoodistoOfConstraint(
    C.singular(C.prop('arvosana')(arviointiSchema))
  )
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialValue =
    props.initialValue?.arvosana && koodiviiteId(props.initialValue.arvosana)
  const selectedValue =
    props.value?.arvosana && koodiviiteId(props.value?.arvosana)

  const onChange = (option?: SelectOption<ArvosanaOf<T>>) => {
    props.onChange(option?.value && props.createArviointi(option.value))
  }

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto as OptionList<ArvosanaOf<T>>}
        onChange={onChange}
        testId={props.testId}
        disabled={props.disabled}
      />
    )
  )
}

export type ParasArvosanaEditProps<T extends Arviointi> = CommonProps<
  FieldEditorProps<T[] | undefined, {}>
> & {
  createArviointi: (arvosana: ArvosanaOf<T>) => T
}

export const ParasArvosanaEdit = <T extends Arviointi>(
  props: ParasArvosanaEditProps<T>
) => {
  const arviointiIndex = parasArviointiIndex(props.initialValue || []) || 0
  const initialArviointi = props.initialValue?.[arviointiIndex]
  const arviointi = props.value?.[arviointiIndex]
  const disabled = (props.value?.length || 0) > 1 // Jos arvioita on useampi, tämä komponentti vain näyttää arvosanan, mutta ei salli editointia

  const onChange = (value?: T) => {
    props.onChange(
      value &&
        updateArvioinnit(
          props.createArviointi(value.arvosana),
          props.initialValue || []
        )
    )
  }

  return (
    <ArvosanaEdit
      initialValue={initialArviointi}
      value={arviointi}
      onChange={onChange}
      createArviointi={props.createArviointi}
      disabled={disabled}
      testId={props.testId}
    />
  )
}

const updateArvioinnit = <T extends Arviointi>(
  arviointi: T,
  arvioinnit: T[]
): T[] => {
  if (arvioinnit.length < 2) {
    return [arviointi]
  }
  console.error(
    'ParasArvosanaEdit ei tue arvioinnin muokkausta, jos suoritukselle on annettu useampi arviointi',
    arvioinnit
  )
  return arvioinnit
}
