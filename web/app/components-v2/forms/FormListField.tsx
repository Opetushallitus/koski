import React from 'react'
import { CommonProps, subTestId } from '../CommonProps'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField,
  FormFieldProps
} from './FormField'
import { FormOptic, getValue } from './FormModel'

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
> = FormFieldProps<FormState, ValueList, ViewComponent, EditComponent>

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
  const values = (getValue(
    props.path as FormOptic<FormState, ValueList | undefined>
  )(props.form.state) || []) as FieldValue[]

  return (
    <>
      {values.map((_, index) => {
        const { form, view, viewProps, edit, editProps, errorsFromPath } = props
        const path = props.path.index(index) as FormOptic<FormState, FieldValue>
        const testId = props.testId && subTestId(props, index.toString())
        return (
          <FormField
            form={form}
            path={path}
            view={view}
            viewProps={viewProps}
            edit={edit}
            editProps={editProps}
            errorsFromPath={errorsFromPath}
            testId={testId}
            index={index}
            key={index}
          />
        )
      })}
    </>
  )
}
