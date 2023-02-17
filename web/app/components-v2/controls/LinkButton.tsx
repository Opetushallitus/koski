import React, { useCallback } from 'react'
import { goto } from '../../util/url'
import { common, CommonPropsWithChildren, testId } from '../CommonProps'

export type LinkButtonProps = CommonPropsWithChildren<{
  href: string
}>

export const LinkButton: React.FC<LinkButtonProps> = (props) => {
  const onClick = useCallback(() => goto(props.href), [props.href])
  return (
    <button
      {...common(props, ['LinkButton'])}
      {...testId(props)}
      onClick={onClick}
    >
      {props.children}
    </button>
  )
}
