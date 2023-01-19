import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useCallback, useState } from 'react'
import { localize, t } from '../../i18n/i18n'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import {
  AnyOrganisaatiohenkilö,
  createOrganisaatiohenkilö
} from '../../util/henkilo'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { Removable } from '../controls/Removable'
import { TextEdit } from '../controls/TextField'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'
import { narrowErrorsToLeaf } from '../forms/validator'

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
      }
    >
  >

export const OrganisaatioHenkilötEdit = <T extends AnyOrganisaatiohenkilö>(
  props: OrganisaatioHenkilötEditProps<T>
): React.ReactElement => {
  const [focusNew, setFocusNew] = useState(false)
  const addNewDisabled = !props.organisaatio

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

  const addNew = useCallback(() => {
    if (props.organisaatio) {
      props.onChange([
        ...(props.value || []),
        createOrganisaatiohenkilö(props.henkilöClass, props.organisaatio) as T
      ])
      setFocusNew(true)
    }
  }, [props.onChange, props.value, props.organisaatio, props.henkilöClass])

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

  return (
    <ul {...common(props, ['ArvioitsijatEdit'])}>
      {props.value &&
        props.value.map((a, i) => (
          <li key={i}>
            <Removable onClick={removeAt(i)}>
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
              />
            </Removable>
          </li>
        ))}
      <li>
        <FlatButton onClick={addNew} disabled={addNewDisabled}>
          lisää uusi
        </FlatButton>
      </li>
    </ul>
  )
}
