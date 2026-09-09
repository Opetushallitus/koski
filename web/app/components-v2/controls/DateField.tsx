import React, { useMemo } from 'react'
import DayPickerInput from 'react-day-picker'
import { useTestId } from '../../appstate/useTestId'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps, cx } from '../CommonProps'
import {
  PositionalPopup,
  PositionalPopupAlign,
  PositionalPopupHolder
} from '../containers/PositionalPopup'
import { FieldErrors } from '../forms/FieldErrors'
import {
  FieldEditorProps,
  FieldViewerProps,
  componentsWithBuiltInErrors
} from '../forms/FormField'
import { IconButton } from './IconButton'
import { useDateEditState } from './useDateEditState'

// Date viewer

export type DateViewProps = CommonProps<FieldViewerProps<string, EmptyObject>>

export const DateView: React.FC<DateViewProps> = (props) => {
  const testId = useTestId(props.testId ? props.testId : 'date.value')
  const formattedDate = useMemo(
    () => (props.value ? ISO2FinnishDate(props.value) : '–'),
    [props.value]
  )

  return (
    <span {...common(props, ['DateView'])} data-testid={testId}>
      {formattedDate}
    </span>
  )
}

// Date editor

export type DateEditProps = CommonProps<
  FieldEditorProps<
    string,
    {
      min?: string
      max?: string
    }
  >
> & {
  align?: PositionalPopupAlign
}

export const DateEdit: React.FC<DateEditProps> = (props) => {
  const {
    displayDate,
    datePickerVisible,
    toggleDayPicker,
    onChange,
    dayPickerProps
  } = useDateEditState(props)
  const testId = props.testId || 'date'
  const inputId = useTestId(`${testId}.edit.input`)
  const buttonId = useTestId(`${testId}.edit.calendarButton`)
  const hasError = Boolean(props.errors)

  return (
    <label {...common(props, ['DateEdit'])}>
      <div className="DateEdit__field">
        <input
          type="text"
          value={displayDate}
          onChange={onChange}
          className={cx(
            'DateEdit__input',
            hasError && 'DateEdit__input--error'
          )}
          data-testid={inputId}
        />
        <PositionalPopupHolder>
          <IconButton
            charCode="f133"
            label={t('Valitse päivämäärä')}
            size="input"
            onClick={toggleDayPicker}
            data-testid={buttonId}
          />
          <PositionalPopup align={props.align} open={datePickerVisible}>
            <DayPickerInput {...dayPickerProps} />
          </PositionalPopup>
        </PositionalPopupHolder>
      </div>
      <FieldErrors errors={props.errors} />
    </label>
  )
}
componentsWithBuiltInErrors.add(DateEdit)

const CalendarButton: React.FC<{
  onClick: React.MouseEventHandler<HTMLAnchorElement>
}> = (props) => (
  <a className="DateEdit__pickerBtn" onClick={props.onClick}>
    {''}
  </a>
)
