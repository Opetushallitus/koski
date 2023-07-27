import React from 'react'
import { common, CommonProps } from '../CommonProps'

export type SpinnerProps = CommonProps<{
  inline?: boolean
  compact?: boolean
}>

export const Spinner: React.FC<SpinnerProps> = (props) => (
  <div
    {...common(props, [
      'spinner',
      props.inline && 'spinner-inline',
      props.compact && 'spinner-compact'
    ])}
  >
    <div className="spinner-circle spinner-circle-green" />
    <div className="spinner-circle spinner-circle-blue" />
  </div>
)
