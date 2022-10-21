import React from 'react'
import * as R from 'ramda'
import { modelData, pushModelValue } from './EditorModel.ts'
import { formatISODate, parseISODate, formatFinnishDate } from '../date/date.js'
import DateInput from '../date/DateInput'
import { wrapOptional, modelSetData } from './EditorModel'

export const DateEditor = ({ model, isAllowedDate }) => {
  const wrappedModel = wrapOptional(model)

  const validityCallback = (valid, stringInput) =>
    !valid && pushModelValue(wrappedModel, { data: stringInput }) // push raw string value to model in case of invalid input. will cause model validation to fail
  const valueCallback = (date) =>
    pushModelValue(
      wrappedModel,
      date && { data: formatISODate(date), title: formatFinnishDate(date) }
    )

  const dateInISOFormat = modelData(wrappedModel)
  const dateValue =
    (dateInISOFormat && parseISODate(dateInISOFormat)) || dateInISOFormat
  return model.context.edit ? (
    <DateInput
      {...{
        value: dateValue,
        optional: isOptional(model),
        isAllowedDate,
        validityCallback,
        valueCallback
      }}
    />
  ) : (
    <span className="inline date">
      {dateValue && formatFinnishDate(dateValue)}
    </span>
  )
}

DateEditor.createEmpty = (d) => modelSetData(d, '')
DateEditor.canShowInline = () => true
DateEditor.handlesOptional = () => true
DateEditor.validateModel = (model) => {
  const data = modelData(model)
  const empty = !data
  if (empty && isOptional(model)) return
  if (!data) return [{ key: 'missing' }]
  const dateValue = data && parseISODate(data)
  if (!dateValue) return [{ key: 'invalid.date' }]
}

const isOptional = (model) =>
  model.optional &&
  !(
    isPerusopetuksenVuosiluokanSuorituksenAlkamispäivä(model) ||
    isEuropeanSchoolOfHelsinkiSuorituksenAlkamispäivä(model)
  )

const isPerusopetuksenVuosiluokanSuorituksenAlkamispäivä = (model) =>
  R.and(
    R.includes('perusopetuksenvuosiluokansuoritus', model.parent.value.classes),
    R.last(model.path) === 'alkamispäivä'
  )

const isEuropeanSchoolOfHelsinkiSuorituksenAlkamispäivä = (model) =>
  R.and(
    R.includes(
      'europeanschoolofhelsinkivuosiluokansuoritus',
      model.parent.value.classes
    ),
    R.last(model.path) === 'alkamispäivä'
  )
