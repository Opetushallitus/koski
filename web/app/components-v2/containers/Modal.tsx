import React, { useCallback, useRef, useEffect } from 'react'
import { useModalState } from '../../appstate/modals'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ButtonGroup } from './ButtonGroup'
import { TestIdLayer } from '../../appstate/useTestId'
import { useOutsideClick } from '../../util/useOutsideClick'

export type ModalProps = CommonPropsWithChildren<{
  onClose?: () => void
}>

export const Modal: React.FC<ModalProps> = (props) => {
  const formRef = useRef<HTMLFormElement>(null)
  const contentRef = useRef<HTMLDivElement>(null)
  const { isActive, props: modalProps } = useModalState()

  useOutsideClick([contentRef], props.onClose, isActive)

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      if (event.key === 'Enter') {
        event.preventDefault()
      } else if (event.key === 'Escape' && isActive) {
        props.onClose?.()
      }
    },
    [isActive, props]
  )

  useEffect(() => {
    if (isActive) {
      // [data-skip-autofocus] -elementit jäävät tab-järjestykseen, mutta
      // niitä ei valita modalin avautuessa automaattisesti.
      const inputs: NodeListOf<HTMLInputElement> | undefined =
        formRef.current?.querySelectorAll(
          'input:not([data-skip-autofocus]), textarea:not([data-skip-autofocus]), button:not([data-skip-autofocus]), [tabindex]:not([data-skip-autofocus])'
        )
      inputs?.[0]?.focus?.()
    }
  }, [isActive])

  return (
    <TestIdLayer id="modal">
      <form
        {...common(props, ['Modal', isActive && 'Modal__active'])}
        {...modalProps}
        onSubmit={(e) => e.preventDefault()}
        onKeyDown={onKeyDown}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        ref={formRef}
      >
        <div className="Modal__content" ref={contentRef}>
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
