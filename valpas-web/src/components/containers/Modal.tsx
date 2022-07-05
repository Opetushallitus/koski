import bem from "bem-ts"
import React, { useEffect, useRef } from "react"
import { t } from "../../i18n/i18n"
import { useComponentAppearDisappear } from "../../state/animations"
import { onKbEscape } from "../../utils/events"
import { FlatButton } from "../buttons/FlatButton"
import { CloseIcon } from "../icons/Icon"
import "./Modal.less"

const b = bem("modal")

export type ModalProps = {
  children: React.ReactNode
  title?: React.ReactNode
  testId?: string
  onClose?: () => void
  closeOnBackgroundClick?: boolean
}

export const Modal = (props: ModalProps) => {
  const animation = useComponentAppearDisappear({
    hideDuration: 200,
    onHidden: props.onClose,
  })

  return (
    <Background
      hidden={animation.hidden}
      onClose={props.closeOnBackgroundClick ? animation.hide : undefined}
    >
      <Container
        hidden={animation.hidden}
        onClose={animation.hide}
        title={props.title}
        testId={props.testId}
      >
        {props.children}
      </Container>
    </Background>
  )
}

type BackgroundProps = {
  hidden: boolean
  children: React.ReactNode
  onClose?: () => void
}

const Background = (props: BackgroundProps) => {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const className = b("noscroll")
    document.body.classList.add(className)
    ref.current?.focus()
    return () => {
      document.body.classList.remove(className)
    }
  })

  return (
    <div
      className={b("background", { init: props.hidden })}
      tabIndex={0}
      role={props.onClose && "button"}
      onClick={props.onClose}
      onKeyDown={onKbEscape(props.onClose)}
      ref={ref}
      aria-hidden="false"
    >
      {props.children}
    </div>
  )
}

type ContainerProps = ModalProps & {
  hidden: boolean
  testId?: string
}

const Container = (props: ContainerProps) => (
  <div
    className={b("container", {
      closeable: !!props.onClose,
      init: props.hidden,
    })}
    role="dialog"
  >
    <div
      className={b("header")}
      data-testid={`${props.testId ? props.testId : ""}__container-header`}
    >
      <h2
        className={b("title")}
        data-testid={`${
          props.testId ? props.testId : ""
        }__container-header-title`}
      >
        {props.title}
      </h2>
      {props.onClose && (
        <FlatButton
          className={b("closebutton")}
          data-testid={`${
            props.testId ? props.testId : ""
          }__container-close-button`}
          onClick={props.onClose}
          aria-label={t("closebutton_aria_label")}
        >
          <CloseIcon />
        </FlatButton>
      )}
    </div>
    <div className={b("content")}>{props.children}</div>
  </div>
)
