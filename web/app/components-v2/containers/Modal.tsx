import React, { useCallback, useRef, useEffect } from 'react'
import { useModalState } from '../../appstate/modals'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ButtonGroup } from './ButtonGroup'
import { TestIdLayer } from '../../appstate/useTestId'

export type ModalProps = CommonPropsWithChildren<{
  onClose?: () => void
}>

export const Modal: React.FC<ModalProps> = (props) => {
  const ref = useRef<HTMLFormElement>(null)
  const { isActive, props: modalProps } = useModalState()

  const onClose = useCallback(
    (event: React.MouseEvent<HTMLFormElement, MouseEvent>) => {
      event.preventDefault()
      event.stopPropagation()
      props.onClose?.()
    },
    [props]
  )

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      if (event.key === 'Enter') {
        event.preventDefault()
      } else if (event.key === 'Escape') {
        props.onClose?.()
      }
    },
    [props]
  )

  useEffect(() => {
    if (isActive) {
      const inputs: NodeListOf<HTMLInputElement> | undefined =
        ref.current?.querySelectorAll('input, textarea, button, [tabindex]')
      inputs?.[0]?.focus?.()
    }
  }, [isActive])

  return (
    <TestIdLayer id="modal">
      <form
        {...common(props, ['Modal', isActive && 'Modal__active'])}
        {...modalProps}
        onSubmit={stopPropagation}
        onKeyDown={onKeyDown}
        onClick={onClose}
        role="dialog"
        ref={ref}
      >
        <div className="Modal__content" onClick={stopPropagation}>
          {props.children}
        </div>
      </form>
    </TestIdLayer>
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
