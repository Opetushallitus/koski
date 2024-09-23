/* eslint-disable no-console */
import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useCallback, useMemo, useState } from 'react'
import { TestIdLayer, TestIdText, useTestId } from '../../appstate/useTestId'
import { localize, t } from '../../i18n/i18n'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import {
  AnyOrganisaatiohenkilö,
  castOrganisaatiohenkilö,
  OrganisaatiohenkilöEq
} from '../../util/henkilo'
import { EmptyObject } from '../../util/objects'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { MultiField } from '../containers/MultiField'
import { Removable } from '../controls/Removable'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TextEdit } from '../controls/TextField'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { narrowErrorsToLeaf } from '../forms/validator'
import { CHARCODE_ADD, IconLabel } from '../texts/Icon'

// Organisaatiohenkilö viewer

export type OrganisaatioHenkilötViewProps<T extends AnyOrganisaatiohenkilö> =
  CommonProps<FieldViewerProps<T[] | undefined, EmptyObject>>

export const OrganisaatioHenkilötView = <T extends AnyOrganisaatiohenkilö>(
  props: OrganisaatioHenkilötViewProps<T>
) => {
  return props.value ? (
    <TestIdLayer id="organisaatiohenkilöt.value">
      <ul {...common(props, ['OrganisaatioHenkilotView'])}>
        {props.value.map((a, i) => (
          <li key={i}>
            <TestIdText id={i}>
              {a.nimi}
              {a.titteli && ` (${t(a.titteli)})`}
            </TestIdText>
          </li>
        ))}
      </ul>
    </TestIdLayer>
  ) : (
    <div {...common(props, ['OrganisaatioHenkilotView'])}>{'–'}</div>
  )
}

// Organisaatiohenkilö editor

export type OrganisaatioHenkilötEditProps<T extends AnyOrganisaatiohenkilö> =
  CommonProps<
    FieldEditorProps<
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
  const state = useOrganisaatioHenkilöState(props)
  const henkilöTestId = useTestId('organisaatiohenkilöt.edit.henkilö')

  return (
    <TestIdLayer id="organisaatiohenkilöt.edit">
      <ul {...common(props, ['ArvioitsijatEdit'])}>
        {(props.value || []).map((hlö, i) => (
          <TestIdLayer key={i} id={`henkilö.${i}`}>
            <li>
              <Removable onClick={state.removeAt(i)}>
                {!props.storedHenkilöt?.find((h) =>
                  OrganisaatiohenkilöEq.equals(hlö, h)
                ) ? (
                  <TestIdLayer id="newHenkilö">
                    <MultiField key={i}>
                      <TextEdit
                        placeholder="Nimi"
                        optional
                        value={hlö.nimi}
                        onChange={state.onChangeNimi(i)}
                        errors={narrowErrorsToLeaf(`${i}.nimi`)(props.errors)}
                        autoFocus={
                          props.value &&
                          i === props.value.length - 1 &&
                          state.focusNew
                        }
                        testId="nimi"
                      />
                      <TextEdit
                        placeholder="Titteli"
                        optional
                        value={t(hlö.titteli)}
                        onChange={state.onChangeTitteli(i)}
                        errors={narrowErrorsToLeaf(`${i}.titteli`)(
                          props.errors
                        )}
                        testId="titteli"
                      />
                    </MultiField>
                  </TestIdLayer>
                ) : (
                  <Select
                    options={state.options}
                    value={hlö.nimi}
                    onChange={state.updateHenkilö(i)}
                    onRemove={state.onRemoveStored}
                    testId="storedHenkilö"
                  />
                )}
              </Removable>
            </li>
          </TestIdLayer>
        ))}
        <li>
          <Select
            options={state.newOptions}
            onChange={state.addHenkilö}
            onRemove={state.onRemoveStored}
            testId="add"
          />
        </li>
      </ul>
    </TestIdLayer>
  )
}

// State

const ADD_NEW_KEY = '__NEW__'

const useOrganisaatioHenkilöState = <T extends AnyOrganisaatiohenkilö>(
  props: OrganisaatioHenkilötEditProps<T>
) => {
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
        removable: true,
        testId: h.nimi
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
                <IconLabel charCode={CHARCODE_ADD}>{'Lisää henkilö'}</IconLabel>
              ),
              testId: 'add'
            }
          ]
        : []),
      ...options
    ],
    [options, props.organisaatio]
  )

  const { onChange, value, organisaatio, henkilöClass } = props
  const addHenkilö = useCallback(
    (option?: SelectOption<T>) => {
      if (option) {
        const newHenkilö =
          option.value ||
          (organisaatio &&
            castOrganisaatiohenkilö(henkilöClass)(
              OrganisaatiohenkilöValinnaisellaTittelillä({
                nimi: '',
                organisaatio: organisaatio
              })
            ))
        if (newHenkilö) {
          onChange([...(value || []), newHenkilö])
          setFocusNew(true)
        }
      }
    },
    [henkilöClass, onChange, organisaatio, value]
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

  const { onRemoveStoredHenkilö } = props
  const onRemoveStored = useCallback(
    (option: SelectOption<T>) => {
      option.value && onRemoveStoredHenkilö(option.value)
    },
    [onRemoveStoredHenkilö]
  )

  return {
    options,
    newOptions,
    addHenkilö,
    focusNew,
    onChangeNimi,
    onChangeTitteli,
    updateHenkilö,
    removeAt,
    onRemoveStored
  }
}
