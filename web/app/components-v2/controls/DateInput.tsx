import React from 'react'
import DayPickerInput from 'react-day-picker'
import { t } from '../../i18n/i18n'
import { cx } from '../CommonProps'
import {
  PositionalPopup,
  PositionalPopupAlign,
  PositionalPopupHolder
} from '../containers/PositionalPopup'
import { IconButton } from './IconButton'
import { useTestId } from '../../appstate/useTestId'
import { useDateEditState } from './useDateEditState'

export type DateInputProps = {
  value?: string
  onChange: (value?: string, rawValue?: string) => void
  min?: string
  max?: string
  testId?: string
  hasErrors?: boolean
} & {
  align?: PositionalPopupAlign
}

export const DateInput: React.FC<DateInputProps> = (props) => {
  const {
    displayDate,
    datePickerVisible,
    toggleDayPicker,
    onChange,
    dayPickerProps
  } = useDateEditState(props)

  const inputId = useTestId(props.testId ? `${props.testId}.input` : 'input')
  const buttonId = useTestId(props.testId ? `${props.testId}.button` : 'button')

  return (
    <div className="DateEdit__field">
      <input
        type="text"
        value={displayDate}
        onChange={onChange}
        className={cx(
          'DateEdit__input',
          props.hasErrors && 'DateEdit__input--error'
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
  )
}
