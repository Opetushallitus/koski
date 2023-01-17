import React, { FormEventHandler, useCallback } from 'react'
import { useModalState } from '../../appstate/modals'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ButtonGroup } from './ButtonGroup'

export type ModalProps = CommonPropsWithChildren<{
  onSubmit?: () => void
  onClose?: () => void
}>

export const Modal: React.FC<ModalProps> = (props) => {
  const { isActive, props: modalProps } = useModalState()
  const onSubmit: FormEventHandler = useCallback(
    (event) => {
      event.preventDefault()
      props.onSubmit?.()
    },
    [props.onSubmit]
  )

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      if (event.key === 'Enter') {
        event.preventDefault()
        props.onSubmit?.()
      } else if (event.key === 'Escape') {
        props.onClose?.()
      }
    },
    [props.onSubmit, props.onClose]
  )

  return (
    <form
      {...common(props, ['Modal', isActive && 'Modal__inactive'])}
      {...modalProps}
      onSubmit={onSubmit}
      onKeyDown={onKeyDown}
      // onClick={props.onClose}
    >
      <div className="Modal__content">{props.children}</div>
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
