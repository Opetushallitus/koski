import React from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate, todayISODate } from '../../date/date'
import { CommonProps } from '../CommonProps'
import { DateInput } from '../controls/DateInput'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { EmptyObject } from '../../util/objects'

export type AikajaksoLike = { alku: string; loppu?: string }

export type AikajaksoViewProps<T extends AikajaksoLike> = CommonProps<
  FieldViewerProps<T | undefined, EmptyObject>
>

export const AikajaksoView = <T extends AikajaksoLike>({
  value
}: AikajaksoViewProps<T>) => {
  return (
    <div>
      <TestIdText id="alku">
        {value?.alku && ISO2FinnishDate(value.alku)}
      </TestIdText>{' '}
      {' — '}
      <TestIdText id="loppu">
        {value?.loppu && ISO2FinnishDate(value.loppu)}
      </TestIdText>
    </div>
  )
}

export type AikajaksoEditProps<T extends AikajaksoLike> = CommonProps<
  FieldEditorProps<
    T,
    {
      createAikajakso: (a: AikajaksoLike) => T | undefined
    }
  >
>

export const AikajaksoEdit = <T extends AikajaksoLike>({
  value,
  onChange,
  createAikajakso,
  testId
}: AikajaksoEditProps<T>) => {
  const setAlku = (alku?: string, rawAlku?: string) => {
    const aikajakso = createAikajakso({
      ...value,
      alku: alku ?? rawAlku ?? ''
    })
    aikajakso && onChange(aikajakso)
  }

  const setLoppu = (loppu?: string, rawLoppu?: string) => {
    const aikajakso = createAikajakso({
      alku: todayISODate(),
      ...value,
      loppu: loppu ?? (rawLoppu ? rawLoppu : undefined)
    })
    aikajakso && onChange(aikajakso)
  }

  return (
    <TestIdLayer id={testId || 'aikajakso'}>
      <div className="AikajaksoEdit">
        <DateInput value={value?.alku} onChange={setAlku} testId="alku" />
        <span className="AikajaksoEdit__separator"> {' — '}</span>
        <DateInput value={value?.loppu} onChange={setLoppu} testId="loppu" />
      </div>
    </TestIdLayer>
  )
}
