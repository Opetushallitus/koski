import bem from "bem-ts"
import React, { useEffect } from "react"
import { useComponentAppearDisappear } from "../../state/animations"
import { FlatButton } from "../buttons/FlatButton"
import { CloseIcon } from "../icons/Icon"
import "./Modal.less"

const b = bem("modal")

export type ModalProps = {
  children: React.ReactNode
  title?: React.ReactNode
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
  useEffect(() => {
    const className = b("noscroll")
    document.body.classList.add(className)
    return () => document.body.classList.remove(className)
  })

  return (
    <div
      className={b("background", { init: props.hidden })}
      onClick={props.onClose}
    >
      {props.children}
    </div>
  )
}

type ContainerProps = ModalProps & {
  hidden: boolean
}

const Container = (props: ContainerProps) => (
  <div
    className={b("container", {
      closeable: !!props.onClose,
      init: props.hidden,
    })}
    onClick={(event) => event.stopPropagation()}
  >
    <div className={b("header")}>
      <h2 className={b("title")}>{props.title}</h2>
      {props.onClose && (
        <FlatButton className={b("closebutton")} onClick={props.onClose}>
          <CloseIcon />
        </FlatButton>
      )}
    </div>
    <div className={b("content")}>{props.children}</div>
  </div>
)
