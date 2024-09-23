/* eslint-disable no-console */
import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useCallback, useState } from 'react'
import { Arvioitsija } from '../../types/fi/oph/koski/schema/Arvioitsija'
import { common, CommonProps } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { Removable } from '../controls/Removable'
import { TextEdit } from '../controls/TextField'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { narrowErrorsToLeaf } from '../forms/validator'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { EmptyObject } from '../../util/objects'

export type ArvioitsijatViewProps = CommonProps<
  FieldViewerProps<Arvioitsija[] | undefined, EmptyObject>
>

export const ArvioitsijatView: React.FC<ArvioitsijatViewProps> = (props) => {
  const testId = useTestId('arvioitsijat.value')
  return props.value && A.isNonEmpty(props.value) ? (
    <ul {...common(props, ['ArvioitsijatView'])} data-testid={testId}>
      {props.value.map((a, i) => (
        <li key={i} data-testid={`${testId}.${i}`}>
          {a.nimi}
        </li>
      ))}
    </ul>
  ) : (
    <span {...common(props, ['ArvioitsijatView'])}>{'–'}</span>
  )
}

export type ArvioitsijatEditProps = CommonProps<
  FieldEditorProps<Arvioitsija[] | undefined, EmptyObject>
>

export const ArvioitsijatEdit: React.FC<ArvioitsijatEditProps> = (props) => {
  const [focusNew, setFocusNew] = useState(false)

  const { onChange, value } = props
  const onChangeCB = (index: number) => (nimi?: string) => {
    pipe(
      value || [],
      A.updateAt(index, Arvioitsija({ nimi: nimi || '' })),
      O.fold(
        () =>
          console.error(
            `Could not add ${nimi} at ${index}, original array:`,
            value
          ),
        onChange
      )
    )
  }

  const addNew = useCallback(() => {
    onChange([...(value || []), Arvioitsija({ nimi: '' })])
    setFocusNew(true)
  }, [onChange, value])

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
    <TestIdLayer id="arvioitsijat.edit">
      <ul {...common(props, ['ArvioitsijatEdit'])}>
        {props.value &&
          props.value.map((a, i) => (
            <li key={i}>
              <TestIdLayer id={i}>
                <Removable onClick={removeAt(i)}>
                  <TextEdit
                    optional
                    value={a.nimi}
                    onChange={onChangeCB(i)}
                    errors={narrowErrorsToLeaf(`${i}.nimi`)(props.errors)}
                    autoFocus={
                      props.value && i === props.value.length - 1 && focusNew
                    }
                  />
                </Removable>
              </TestIdLayer>
            </li>
          ))}
        <li>
          <FlatButton onClick={addNew} testId="addNew">
            {t('lisää uusi')}
          </FlatButton>
        </li>
      </ul>
    </TestIdLayer>
  )
}
