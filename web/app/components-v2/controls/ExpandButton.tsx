import React from 'react'

export type ExpandButtonProps = {
  expanded: boolean
  onChange: (expanded: boolean) => void
  label: string
  disabled?: boolean
}

export const ExpandButton: React.FC<ExpandButtonProps> = (props) => (
  <a
    className="toggle-expand"
    onClick={() => props.onChange(!props.expanded)}
    role="button"
    aria-expanded={false}
    aria-label={
      props.expanded ? `Pienennä ${props.label}` : `Laajenna ${props.label}`
    }
  >
    {props.expanded ? <>&#61766;</> : <>&#61694;</>}
  </a>
)
