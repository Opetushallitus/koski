import React, { FormEventHandler, useCallback } from 'react'
import { useModalState } from '../../appstate/modals'
import { baseProps, BaseProps } from '../baseProps'
import { ButtonGroup } from './ButtonGroup'

export type ModalProps = React.PropsWithChildren<
  BaseProps & {
    onSubmit?: () => void
    onClose?: () => void
  }
>

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
      {...baseProps(props, 'Modal', isActive && 'Modal__inactive')}
      {...modalProps}
      onSubmit={onSubmit}
      onKeyDown={onKeyDown}
    >
      <div className="Modal__content">{props.children}</div>
    </form>
  )
}

export type ModalTitleProps = React.PropsWithChildren<BaseProps>

export const ModalTitle: React.FC<ModalTitleProps> = (props) => (
  <h1 {...baseProps(props, 'ModalTitle')}>{props.children}</h1>
)

export type ModalBodyProps = React.PropsWithChildren<BaseProps>

export const ModalBody: React.FC<ModalBodyProps> = (props) => (
  <section {...baseProps(props, 'ModalBody')}>{props.children}</section>
)

export type ModalFooterProps = React.PropsWithChildren<BaseProps>

export const ModalFooter: React.FC<ModalFooterProps> = (props) => (
  <section {...baseProps(props, 'ModalFooter')}>
    <ButtonGroup>{props.children}</ButtonGroup>
  </section>
)
