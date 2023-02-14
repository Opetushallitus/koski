import React, { useCallback, useEffect, useRef } from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type PositionalPopupAlign = 'left' | 'right'

export type PositionalPopupProps = CommonPropsWithChildren<{
  onDismiss?: () => void
  align?: PositionalPopupAlign
}>

export const PositionalPopup: React.FC<PositionalPopupProps> = (props) => {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (props.onDismiss) {
      const clickHandler = (event: MouseEvent) => {
        if (
          !(event.target instanceof Element) ||
          !ref.current?.contains(event.target)
        ) {
          props.onDismiss?.()
        }
      }
      document.body.addEventListener('click', clickHandler)
      return () => document.body.removeEventListener('click', clickHandler)
    }
  }, [props, props.onDismiss])

  return (
    <div
      {...common(props, [
        'PositionalPopup',
        props.align && `PositionalPopup--align-${props.align}`
      ])}
      ref={ref}
    >
      <div className="PositionalPopup__overlay">{props.children}</div>
    </div>
  )
}

export type PositionalPopupHolderProps = CommonPropsWithChildren

export const PositionalPopupHolder: React.FC<PositionalPopupHolderProps> = (
  props
) => <div {...common(props, ['PositionalPopupHolder'])}>{props.children}</div>
