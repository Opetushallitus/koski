import React, { useMemo } from 'react'
import { useConstraint } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { constraintObjectProp } from '../../util/constraints'
import { toKoodistokoodiviiteValue } from '../../util/koodisto'
import { ArviointiLike, viimeisinArviointi } from '../../util/schema'
import { schemaClassName } from '../../util/types'
import { baseProps, BaseProps } from '../baseProps'
import {
  groupKoodistoToOptions,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormModel'

export type ArviointiViewProps<T extends ArviointiLike> = BaseProps &
  FieldViewBaseProps<T[]>

export const ArviointiView = <T extends ArviointiLike>(
  props: ArviointiViewProps<T>
) => {
  const arviointi = props.value && viimeisinArviointi(props.value)
  return arviointi ? (
    <span {...baseProps(props)}>{t(arviointi.arvosana?.nimi)}</span>
  ) : null
}

export type ArviointiEditProps<T extends ArviointiLike> = BaseProps &
  FieldEditBaseProps<T[]> & {
    createArviointi: (arvosana: T['arvosana']) => T
  }

export const ArviointiEdit = <T extends ArviointiLike>(
  props: ArviointiEditProps<T>
) => {
  const schemaClass = useMemo(
    // @ts-ignore - koska value ja initialValue voivat olla tyhjiä, saadaan $class varmuudella selvitettyä syöttämällä createArviointi-callbackille tyhjä arvosana
    () => schemaClassName(props.createArviointi(null).$class),
    []
  )
  const arviointiC = useConstraint(schemaClass)
  const koodisto = useKoodistoOfConstraint(
    constraintObjectProp(arviointiC, 'arvosana')
  )
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialArviointi =
    props.initialValue && viimeisinArviointi(props.initialValue)
  const initialValue =
    initialArviointi?.arvosana &&
    toKoodistokoodiviiteValue(initialArviointi.arvosana)
  const arviointi = props.value && viimeisinArviointi(props.value)
  const selectedValue =
    arviointi?.arvosana && toKoodistokoodiviiteValue(arviointi?.arvosana)

  const onChange = (option?: SelectOption<T['arvosana']>) => {
    props.onChange(
      option
        ? [...(props.initialValue || []), props.createArviointi(option.value)]
        : []
    )
  }

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto}
        onChange={onChange}
      />
    )
  )
}
