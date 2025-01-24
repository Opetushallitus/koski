import React from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField,
  FormFieldProps
} from './FormField'
import { FormOptic, getValue } from './FormModel'
import { Removable } from '../controls/Removable'
import { deleteAt } from '../../util/array'

export type FormListFieldProps<
  FormState extends object,
  ValueList extends FieldValue[] | undefined,
  ViewComponent extends React.FunctionComponent<
    FieldViewerProps<FieldValue, any>
  >,
  EditComponent extends React.FunctionComponent<
    FieldEditorProps<FieldValue, any>
  >,
  FieldValue
> = FormFieldProps<FormState, ValueList, ViewComponent, EditComponent> & {
  removable?: boolean
}

export const FormListField = <
  FormState extends object,
  ValueList extends FieldValue[] | undefined,
  ViewComponent extends React.FunctionComponent<
    FieldViewerProps<FieldValue, any>
  >,
  EditComponent extends React.FunctionComponent<
    FieldEditorProps<FieldValue, any>
  >,
  FieldValue
>(
  props: FormListFieldProps<
    FormState,
    ValueList,
    ViewComponent,
    EditComponent,
    FieldValue
  >
) => {
  const path = props.path as FormOptic<FormState, ValueList | undefined>
  const values = (getValue(path)(props.form.state) || []) as FieldValue[]

  const remove = (index: number) => () => {
    props.form.updateAt(
      path,
      (ts) => (ts ? deleteAt(index)(ts) : undefined) as ValueList
    )
  }

  return (
    <>
      {values.map((_, index) => {
        const {
          form,
          view,
          viewProps,
          edit,
          editProps,
          errorsFromPath,
          testId,
          removable
        } = props
        const valuePath = props.path.index(index) as FormOptic<
          FormState,
          FieldValue
        >
        return (
          <TestIdLayer key={index} id={index}>
            <Removable
              isRemovable={Boolean(form.editMode && removable)}
              onClick={remove(index)}
            >
              <FormField
                form={form}
                path={valuePath}
                view={view}
                viewProps={viewProps}
                edit={edit}
                editProps={editProps}
                errorsFromPath={errorsFromPath}
                testId={testId}
                index={index}
              />
            </Removable>
          </TestIdLayer>
        )
      })}
    </>
  )
}
