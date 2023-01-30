import React, { useCallback } from 'react'
import { useModalState } from '../../appstate/modals'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ButtonGroup } from './ButtonGroup'

export type ModalProps = CommonPropsWithChildren<{
  onClose?: () => void
}>

export const Modal: React.FC<ModalProps> = (props) => {
  const { isActive, props: modalProps } = useModalState()
  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      if (event.key === 'Enter') {
        event.preventDefault()
      } else if (event.key === 'Escape') {
        props.onClose?.()
      }
    },
    [props.onClose]
  )

  return (
    <form
      {...common(props, ['Modal', isActive && 'Modal__inactive'])}
      {...modalProps}
      onSubmit={stopPropagation}
      onKeyDown={onKeyDown}
      onClick={props.onClose}
    >
      <div className="Modal__content" onClick={stopPropagation}>
        {props.children}
      </div>
    </form>
  )
}

export type ModalTitleProps = CommonPropsWithChildren

export const ModalTitle: React.FC<ModalTitleProps> = (props) => (
  <h1 {...common(props, ['ModalTitle'])}>{props.children}</h1>
)

export type ModalBodyProps = CommonPropsWithChildren

export const ModalBody: React.FC<ModalBodyProps> = (props) => (
  <section {...common(props, ['ModalBody'])}>{props.children}</section>
)

export type ModalFooterProps = CommonPropsWithChildren

export const ModalFooter: React.FC<ModalFooterProps> = (props) => (
  <section {...common(props, ['ModalFooter'])}>
    <ButtonGroup>{props.children}</ButtonGroup>
  </section>
)

const stopPropagation: React.EventHandler<any> = (event) => {
  event.preventDefault()
  event.stopPropagation()
}
