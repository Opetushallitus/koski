import React, { useCallback, useMemo } from 'react'
import { common, CommonProps } from '../CommonProps'
import { Icon } from '../texts/Icon'
import { useTestId } from '../../appstate/useTestId'

export type IconButtonSize = 'normal' | 'input'

export type IconButtonProps = CommonProps<{
  charCode: string
  label: string
  onClick: React.MouseEventHandler<HTMLButtonElement>
  size?: IconButtonSize
  testId?: string
}>

export const IconButton: React.FC<IconButtonProps> = (props) => {
  const testId = useTestId(props.testId)
  const { onClick } = props
  const onClickCB: React.MouseEventHandler<HTMLButtonElement> = useCallback(
    (event) => {
      event.preventDefault()
      event.stopPropagation()
      onClick(event)
    },
    [onClick]
  )

  return (
    <button
      {...common(props, [
        'IconButton',
        props.size && `IconButton--size-${props.size}`
      ])}
      data-testid={testId}
      onClick={onClickCB}
      aria-label={props.label}
      title={props.label}
    >
      <Icon charCode={props.charCode} />
    </button>
  )
}
