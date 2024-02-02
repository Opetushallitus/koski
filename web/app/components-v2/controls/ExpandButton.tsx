import React, { PropsWithChildren } from 'react'
import { common, CommonProps } from '../CommonProps'
import { CHARCODE_CLOSE, CHARCODE_OPEN, Icon } from '../texts/Icon'
import { useTestId } from '../../appstate/useTestId'

export type ExpandButtonProps = CommonProps<
  PropsWithChildren<{
    expanded: boolean
    onChange: (expanded: boolean) => void
    label: string
    disabled?: boolean
  }>
>

export const ExpandButton: React.FC<ExpandButtonProps> = (props) => {
  const testId = useTestId('expand')

  const classNames = ['ExpandButton'].concat(
    props.children ? ['haschildren'] : []
  )

  return (
    <button
      {...common(props, classNames)}
      onClick={() => props.onChange(!props.expanded)}
      role="button"
      aria-expanded={false}
      aria-label={
        props.expanded ? `Pienennä ${props.label}` : `Laajenna ${props.label}`
      }
      data-testid={testId}
    >
      {props.children ? (
        props.children
      ) : (
        <ExpandButtonIcon expanded={props.expanded} />
      )}
    </button>
  )
}

export type ExpandButtonIconProps = CommonProps<{
  expanded: boolean
}>

export const ExpandButtonIcon: React.FC<ExpandButtonIconProps> = (props) => {
  return <Icon charCode={props.expanded ? CHARCODE_CLOSE : CHARCODE_OPEN} />
}
