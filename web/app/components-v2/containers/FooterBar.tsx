import React, { useEffect } from 'react'
import { baseProps, BaseProps } from '../baseProps'
import { ContentContainer } from './ContentContainer'

export type FooterBarProps = React.PropsWithChildren<BaseProps>

export const FooterBar: React.FC<FooterBarProps> = (props) => {
  useEffect(() => {
    document.body.classList.add('FooterBar--visible')
    return () => document.body.classList.remove('FooterBar--visible')
  }, [])

  return (
    <footer {...baseProps(props, 'FooterBar')}>
      <ContentContainer>{props.children}</ContentContainer>
    </footer>
  )
}
