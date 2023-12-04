import React from 'react'
import { common, CommonProps } from '../CommonProps'
import { CHARCODE_CLOSE, CHARCODE_OPEN, Icon } from '../texts/Icon'
import { useTestId } from '../../appstate/useTestId'

export type ExpandButtonProps = CommonProps<{
  expanded: boolean
  onChange: (expanded: boolean) => void
  label: string
  disabled?: boolean
}>

export const ExpandButton: React.FC<ExpandButtonProps> = (props) => {
  const testId = useTestId('expand')
  return (
    <button
      {...common(props, ['ExpandButton'])}
      onClick={() => props.onChange(!props.expanded)}
      role="button"
      aria-expanded={false}
      aria-label={
        props.expanded ? `Pienennä ${props.label}` : `Laajenna ${props.label}`
      }
      data-testid={testId}
    >
      <Icon charCode={props.expanded ? CHARCODE_CLOSE : CHARCODE_OPEN} />
    </button>
  )
}
