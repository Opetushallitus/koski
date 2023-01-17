import React, { useMemo } from 'react'
import { common, CommonProps } from '../CommonProps'

export type IconButtonSize = 'normal' | 'input'

export type IconButtonProps = CommonProps<{
  charCode: string
  label: string
  onClick: () => void
  size?: IconButtonSize
}>

export const IconButton: React.FC<IconButtonProps> = (props) => {
  const icon = useMemo(
    () => String.fromCharCode(parseInt(props.charCode, 16)),
    [props.charCode]
  )

  return (
    <button
      {...common(props, [
        'IconButton',
        props.size && `IconButton--size-${props.size}`
      ])}
      onClick={props.onClick}
      aria-label={props.label}
      title={props.label}
    >
      {icon}
    </button>
  )
}

export const CHARCODE_REMOVE = 'f1f8'
