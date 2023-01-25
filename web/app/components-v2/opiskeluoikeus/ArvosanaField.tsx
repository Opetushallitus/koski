import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useMemo } from 'react'
import { useSchema } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { prop } from '../../util/constraints'
import {
  KoodistoUriOf,
  koodiviiteId,
  KoodiviiteWithOptionalUri
} from '../../util/koodisto'
import { viimeisinArviointi } from '../../util/schema'
import { schemaClassName } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import {
  groupKoodistoToOptions,
  OptionList,
  Select,
  SelectOption
} from '../controls/Select'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'

type ArvosanaOf<T extends Arviointi> = Exclude<
  T['arvosana'],
  KoodiviiteWithOptionalUri
>

export type ArviointiViewProps<T extends Arviointi> = CommonProps<
  FieldViewBaseProps<T[] | undefined>
>

export const ArvosanaView = <T extends Arviointi>(
  props: ArviointiViewProps<T>
) => {
  const arviointi = props.value && viimeisinArviointi(props.value)
  return arviointi ? (
    <span {...common(props)}>{t(arviointi.arvosana?.nimi)}</span>
  ) : null
}

export type ArviointiEditProps<T extends Arviointi> = CommonProps<
  FieldEditBaseProps<T[] | undefined> & {
    createArviointi: (arvosana: ArvosanaOf<T>) => T
  }
>

export const ArvosanaEdit = <T extends Arviointi>(
  props: ArviointiEditProps<T>
) => {
  const schemaClass = useMemo(
    // @ts-ignore - koska value ja initialValue voivat olla tyhjiä, saadaan $class varmuudella selvitettyä syöttämällä createArviointi-callbackille tyhjä arvosana
    () => schemaClassName(props.createArviointi(null).$class),
    []
  )
  const arviointiSchema = useSchema(schemaClass)
  const koodisto = useKoodistoOfConstraint(prop('arvosana')(arviointiSchema))
  const groupedKoodisto = useMemo(
    () => koodisto && groupKoodistoToOptions(koodisto),
    [koodisto]
  )

  const initialArviointi =
    props.initialValue && viimeisinArviointi(props.initialValue)
  const initialValue =
    initialArviointi?.arvosana && koodiviiteId(initialArviointi.arvosana)
  const arviointi = props.value && viimeisinArviointi(props.value)
  const selectedValue = arviointi?.arvosana && koodiviiteId(arviointi?.arvosana)

  const onChange = (option?: SelectOption<ArvosanaOf<T>>) => {
    props.onChange(
      option?.value &&
        updateArvioinnit(
          props.createArviointi(option.value),
          props.initialValue || []
        )
    )
  }

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto as OptionList<ArvosanaOf<T>>}
        onChange={onChange}
      />
    )
  )
}

const updateArvioinnit = <T extends Arviointi>(
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
