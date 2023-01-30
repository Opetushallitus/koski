import React, { useCallback, useMemo } from 'react'
import { common, CommonProps } from '../CommonProps'
import { Icon } from '../texts/Icon'

export type IconButtonSize = 'normal' | 'input'

export type IconButtonProps = CommonProps<{
  charCode: string
  label: string
  onClick: React.MouseEventHandler<HTMLButtonElement>
  size?: IconButtonSize
}>

export const IconButton: React.FC<IconButtonProps> = (props) => {
  const onClick: React.MouseEventHandler<HTMLButtonElement> = useCallback(
    (event) => {
      event.preventDefault()
      event.stopPropagation()
      props.onClick(event)
    },
    [props.onClick]
  )

  return (
    <button
      {...common(props, [
        'IconButton',
        props.size && `IconButton--size-${props.size}`
      ])}
      onClick={onClick}
      aria-label={props.label}
      title={props.label}
    >
      <Icon charCode={props.charCode} />
    </button>
  )
}
