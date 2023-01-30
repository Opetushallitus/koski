import React, { useEffect } from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ContentContainer } from './ContentContainer'

export type FooterBarProps = CommonPropsWithChildren

export const FooterBar: React.FC<FooterBarProps> = (props) => {
  useEffect(() => {
    document.body.classList.add('FooterBar--visible')
    return () => document.body.classList.remove('FooterBar--visible')
  }, [])

  return (
    <footer {...common(props, ['FooterBar'])}>
      <ContentContainer>{props.children}</ContentContainer>
    </footer>
  )
}
