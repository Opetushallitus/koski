import React, {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useEffect
} from 'react'
import { useModalState } from '../../appstate/modals'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { ButtonGroup } from './ButtonGroup'
import { TestIdLayer } from '../../appstate/useTestId'
import { useOutsideClick } from '../../util/useOutsideClick'

export type ModalProps = CommonPropsWithChildren<{
  onClose?: () => void
}>

// Antaa ModalTitlelle satunnaisen id:n, johon role="dialog" -elementti
// viittaa aria-labelledby:llä - ilman tätä ruudunlukija ei osaa nimetä
// modaalia lainkaan (vahvistettu axe-core:lla: "aria-label attribute does
// not exist..." jokaisessa Modalissa, koska mikään ei aiemmin kytkenyt
// näkyvää otsikkoa dialogin rooliin). Sama Context+useMemo(Math.random())
// -malli kuin KeyValueTable.tsx:n KeyValueRowLabelContextissa.
const ModalTitleContext = createContext<string | undefined>(undefined)

export const Modal: React.FC<ModalProps> = (props) => {
  const dialogRef = useRef<HTMLDivElement>(null)
  const contentRef = useRef<HTMLDivElement>(null)
  const { isActive, props: modalProps } = useModalState()
  const titleId = useMemo(() => Math.random().toString(), [])

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
        dialogRef.current?.querySelectorAll(
          'input:not([data-skip-autofocus]), textarea:not([data-skip-autofocus]), button:not([data-skip-autofocus]), [tabindex]:not([data-skip-autofocus])'
        )
      inputs?.[0]?.focus?.()
    }
  }, [isActive])

  return (
    <TestIdLayer id="modal">
      {/* <div>, ei <form> - role="dialog" ei ole sallittu <form>-elementille
          (ARIA in HTML -spesifikaation implisiittinen rooliristiriita,
          vahvistettu axe-core:lla: "ARIA role dialog is not allowed for
          given element"). onSubmit ei siis enää tarvita - Enterin
          oletuskäyttäytyminen (lomakkeen lähetys) ei koske <div>:iä
          ylipäätään, ja onKeyDown hoitaa Enterin erikseen silti. */}
      <div
        {...common(props, ['Modal', isActive && 'Modal__active'])}
        {...modalProps}
        onKeyDown={onKeyDown}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-labelledby={titleId}
        ref={dialogRef}
      >
        <div className="Modal__content" ref={contentRef}>
          <ModalTitleContext.Provider value={titleId}>
            {props.children}
          </ModalTitleContext.Provider>
        </div>
      </div>
    </TestIdLayer>
  )
}

export type ModalTitleProps = CommonPropsWithChildren

export const ModalTitle: React.FC<ModalTitleProps> = (props) => {
  const titleId = useContext(ModalTitleContext)
  return (
    <h1 id={titleId} {...common(props, ['ModalTitle'])}>
      {props.children}
    </h1>
  )
}

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
