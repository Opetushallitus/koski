import React, { useCallback } from 'react'
import { navigate } from '../../util/router'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { useTestId } from '../../appstate/useTestId'

export type LinkButtonProps = CommonPropsWithChildren<{
  href: string
  testId?: string | number
}>

export const LinkButton: React.FC<LinkButtonProps> = (props) => {
  const testId = useTestId(props.testId)
  const onClick = useCallback(() => navigate(props.href), [props.href])
  return (
    <button
      {...common(props, ['LinkButton'])}
      onClick={onClick}
      data-testid={testId}
    >
      {props.children}
    </button>
  )
}
