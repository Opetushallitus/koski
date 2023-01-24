import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useCallback, useMemo, useState } from 'react'
import { localize, t } from '../../i18n/i18n'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import {
  AnyOrganisaatiohenkilö,
  castOrganisaatiohenkilö,
  OrganisaatiohenkilöEq
} from '../../util/henkilo'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { MultiField } from '../containers/MultiField'
import { Removable } from '../controls/Removable'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TextEdit } from '../controls/TextField'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'
import { narrowErrorsToLeaf } from '../forms/validator'
import { CHARCODE_ADD, IconLabel } from '../texts/Icon'

const ADD_NEW_KEY = '__NEW__'

export type OrganisaatioHenkilötViewProps<T extends AnyOrganisaatiohenkilö> =
  CommonProps<FieldViewBaseProps<T[] | undefined>>

export const OrganisaatioHenkilötView = <T extends AnyOrganisaatiohenkilö>(
  props: OrganisaatioHenkilötViewProps<T>
) => {
  return props.value ? (
    <ul {...common(props, ['OrganisaatioHenkilotView'])}>
      {props.value.map((a, i) => (
        <li key={i}>
          {a.nimi}
          {a.titteli && ` (${t(a.titteli)})`}
        </li>
      ))}
    </ul>
  ) : (
    <div {...common(props, ['OrganisaatioHenkilotView'])}>–</div>
  )
}

export type OrganisaatioHenkilötEditProps<T extends AnyOrganisaatiohenkilö> =
  CommonProps<
    FieldEditBaseProps<
      T[] | undefined,
      {
        henkilöClass: ClassOf<T>
        organisaatio?: Organisaatio
        storedHenkilöt?: T[]
        onRemoveStoredHenkilö: (henkilö: T) => void
      }
    >
  >

export const OrganisaatioHenkilötEdit = <T extends AnyOrganisaatiohenkilö>(
  props: OrganisaatioHenkilötEditProps<T>
): React.ReactElement => {
  const [focusNew, setFocusNew] = useState(false)

  const onChangeNimi = (index: number) => (nimi?: string) => {
    pipe(
      props.value || [],
      A.modifyAt(index, (o) => ({ ...o, nimi: nimi || '' })),
      O.fold(
        () =>
          console.error(
            `Could not add 'nimi' ${nimi} at ${index}, original array:`,
            props.value
          ),
        props.onChange
      )
    )
  }

  const onChangeTitteli = (index: number) => (titteli?: string) => {
    pipe(
      props.value || [],
      A.modifyAt(index, (o) => ({
        ...o,
        titteli: titteli ? localize(titteli) : undefined
      })),
      O.fold(
        () =>
          console.error(
            `Could not add 'titteli' ${titteli} at ${index}, original array:`,
            props.value
          ),
        props.onChange
      )
    )
  }

  const removeAt = (index: number) => () => {
    pipe(
      props.value || [],
      A.deleteAt(index),
      O.fold(
        () =>
          console.error(
            `Could not remove at ${index}, original array:`,
            props.value
          ),
        props.onChange
      )
    )
  }

  const options: OptionList<T> | undefined = useMemo(
    () =>
      props.storedHenkilöt?.map((h) => ({
        key: h.nimi,
        label: `${h.nimi}${h.titteli ? ` (${t(h.titteli)})` : ''}`,
        value: h,
        removable: true
      })) || [],
    [props.storedHenkilöt]
  )

  const newOptions: OptionList<T> | undefined = useMemo(
    () => [
      ...(props.organisaatio
        ? [
            {
              key: ADD_NEW_KEY,
              label: 'Lisää henkilö',
              display: (
                <IconLabel charCode={CHARCODE_ADD}>Lisää henkilö</IconLabel>
              )
            }
          ]
        : []),
      ...options
    ],
    [options]
  )

  const addHenkilö = useCallback(
    (option?: SelectOption<T>) => {
      if (option) {
        const newHenkilö =
          option.value ||
          castOrganisaatiohenkilö(props.henkilöClass)(
            OrganisaatiohenkilöValinnaisellaTittelillä({
              nimi: '',
              organisaatio: props.organisaatio!
            })
          )
        props.onChange([...(props.value || []), newHenkilö])
        setFocusNew(true)
      }
    },
    [props.value]
  )

  const updateHenkilö = (index: number) => (option?: SelectOption<T>) => {
    if (option && option.value) {
      pipe(
        props.value || [],
        A.updateAt(index, option.value),
        O.map(props.onChange)
      )
    }
  }

  const onRemoveStored = useCallback(
    (option: SelectOption<T>) => {
      option.value && props.onRemoveStoredHenkilö(option.value)
    },
    [props.onRemoveStoredHenkilö]
  )

  return (
    <ul {...common(props, ['ArvioitsijatEdit'])}>
      {(props.value || []).map((a, i) => (
        <li key={i}>
          {!props.storedHenkilöt?.find((h) =>
            OrganisaatiohenkilöEq.equals(a, h)
          ) ? (
            <MultiField key={i}>
              <TextEdit
                placeholder="Nimi"
                optional
                value={a.nimi}
                onChange={onChangeNimi(i)}
                errors={narrowErrorsToLeaf(`${i}.nimi`)(props.errors)}
                autoFocus={
                  props.value && i === props.value.length - 1 && focusNew
                }
              />
              <TextEdit
                placeholder="Titteli"
                optional
                value={t(a.titteli)}
                onChange={onChangeTitteli(i)}
                errors={narrowErrorsToLeaf(`${i}.titteli`)(props.errors)}
                allowEmpty={
                  props.henkilöClass ===
                  'fi.oph.koski.schema.OrganisaatiohenkilöValinnaisellaTittelillä'
                }
              />
            </MultiField>
          ) : (
            <Removable onClick={removeAt(i)}>
              <Select
                options={options}
                value={a.nimi}
                onChange={updateHenkilö(i)}
                onRemove={onRemoveStored}
              />
            </Removable>
          )}
        </li>
      ))}
      <li>
        <Select
          options={newOptions}
          onChange={addHenkilö}
          onRemove={onRemoveStored}
        />
      </li>
    </ul>
  )
}
