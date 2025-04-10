import { flow, pipe } from 'fp-ts/lib/function'
import React, { useCallback, useMemo } from 'react'
import { useChildSchema } from '../../appstate/constraints'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { TestIdText } from '../../appstate/useTestId'
import { todayISODate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import { isObjectConstraint } from '../../types/fi/oph/koski/typemodel/ObjectConstraint'
import { parasArviointi, parasArviointiIndex } from '../../util/arvioinnit'
import * as C from '../../util/constraints'
import { koodiviiteId } from '../../util/koodisto'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import {
  groupKoodistoToOptions,
  mapOptionLabels,
  OptionList,
  Select,
  SelectOption,
  sortOptions
} from '../controls/Select'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { DateView } from '../controls/DateField'

type ArvosanaOf<T extends Arviointi> = T['arvosana']

export type ArvosanaViewProps<T extends Arviointi> = CommonProps<
  FieldViewerProps<T | undefined, EmptyObject>
>

export const ArvosanaView = <T extends Arviointi>(
  props: ArvosanaViewProps<T>
) => (
  <TestIdText {...props} id="arvosana.value">
    {props.value ? t(props.value.arvosana?.nimi) : '-'}
  </TestIdText>
)

export type ParasArvosanaViewProps<T extends Arviointi> = CommonProps<
  FieldViewerProps<T[] | undefined, EmptyObject>
>

export const ParasArvosanaView = <T extends Arviointi>(
  props: ParasArvosanaViewProps<T>
) => {
  const paras = props.value !== undefined && parasArviointi(props.value)
  return paras ? <ArvosanaView {...props} value={paras} /> : null
}

export const ParasArvosanaPäiväView = <T extends Arviointi>(
  props: ParasArvosanaViewProps<T>
) => {
  const paras = props.value !== undefined && parasArviointi(props.value)
  return paras ? <DateView {...props} value={(paras as any).päivä} /> : null
}

export type ArvosanaEditProps<T extends Arviointi> = CommonProps<
  FieldEditorProps<T | undefined, EmptyObject>
> & {
  suoritusClassName: string
  arviointiPropName?: string
  disabled?: boolean
  format?: (arvosana: Koodistokoodiviite) => string
}

export const koodiarvoAndNimi = (k: Koodistokoodiviite) =>
  `${k.koodiarvo} (${t(k.nimi)})`
export const koodiarvoOnly = (k: Koodistokoodiviite) => k.koodiarvo
export const koodinNimiOnly = (k: Koodistokoodiviite) =>
  t(k.nimi) || k.koodiarvo

export const ArvosanaEdit = <T extends Arviointi>(
  props: ArvosanaEditProps<T>
) => {
  if (!props.suoritusClassName) {
    console.error('ArvosanaEdit property suoritusClassName missing')
  }

  const arviointiSchema = useChildSchema(
    props.suoritusClassName,
    `${props.arviointiPropName || 'arviointi'}.[]`
  )
  const arvosanaSchema = useMemo(
    () => pipe(arviointiSchema, C.prop('arvosana'), C.join),
    [arviointiSchema]
  )

  const createArviointi = useCreateDefaultArviointi(arviointiSchema)
  const koodisto = useKoodistoOfConstraint(arvosanaSchema)
  const groupedKoodisto = useMemo(
    () =>
      koodisto &&
      pipe(
        groupKoodistoToOptions(koodisto),
        mapOptionLabels((o) =>
          o.value ? (props.format || koodiarvoAndNimi)(o.value as any) : o.label
        ),
        sortOptions
      ),
    [koodisto, props]
  )

  const initialValue =
    props.initialValue?.arvosana && koodiviiteId(props.initialValue.arvosana)
  const selectedValue =
    props.value?.arvosana && koodiviiteId(props.value?.arvosana)

  const onChange = useCallback(
    (option?: SelectOption<ArvosanaOf<T>>) => {
      props.onChange(
        createArviointi(option as SelectOption<Koodistokoodiviite>) as T
      )
    },
    [createArviointi, props]
  )

  return (
    groupedKoodisto && (
      <Select
        initialValue={initialValue}
        value={selectedValue}
        options={groupedKoodisto as OptionList<ArvosanaOf<T>>}
        onChange={onChange}
        disabled={props.disabled}
        testId={`${props.testId || 'arvosana'}.edit`}
      />
    )
  )
}

const useCreateDefaultArviointi = (arviointiSchema: Constraint | null) => {
  return useCallback(
    (option?: SelectOption<Koodistokoodiviite>): Arviointi | undefined => {
      const arvosana = option?.value
      if (arvosana) {
        const exactArviointiSchema = findArviointiSchema(
          arvosana,
          arviointiSchema
        )
        if (isObjectConstraint(exactArviointiSchema)) {
          const arviointi: Record<string, any> = {
            $class: exactArviointiSchema.class,
            arvosana
          }
          if (C.hasProp(exactArviointiSchema, 'päivä')) {
            arviointi.päivä = todayISODate()
          }
          return arviointi as any
        }
      }
    },
    [arviointiSchema]
  )
}

const findArviointiSchema = (
  arvosana: Koodistokoodiviite,
  arviointiSchema: Constraint | null
) =>
  pipe(
    arviointiSchema,
    C.asList,
    C.filter(
      flow(
        C.prop('arvosana'),
        C.filterKoodistokoodiviite(arvosana.koodistoUri, [arvosana.koodiarvo]),
        C.isNonEmpty
      )
    ),
    C.singular
  )

export type ParasArvosanaEditProps<T extends Arviointi> = CommonProps<
  FieldEditorProps<T[] | undefined, EmptyObject>
> & {
  suoritusClassName: string
  format?: (arvosana: Koodistokoodiviite) => string
}

export const ParasArvosanaEdit = <T extends Arviointi>(
  props: ParasArvosanaEditProps<T>
) => {
  const arviointiIndex = parasArviointiIndex(props.initialValue || []) || 0
  const initialArviointi = props.initialValue?.[arviointiIndex]
  const arviointi = props.value?.[arviointiIndex]
  const disabled = (props.value?.length || 0) > 1 // Jos arvioita on useampi, tämä komponentti vain näyttää arvosanan, mutta ei salli editointia

  const onChange = (value?: T) => {
    props.onChange(value && updateArvioinnit(value, props.initialValue || []))
  }

  return (
    <ArvosanaEdit
      initialValue={initialArviointi}
      value={arviointi}
      onChange={onChange}
      suoritusClassName={props.suoritusClassName}
      format={props.format}
      disabled={disabled}
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
