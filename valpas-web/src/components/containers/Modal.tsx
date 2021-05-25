import bem from "bem-ts"
import React, { useEffect } from "react"
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

export const Modal = (props: ModalProps) => (
  <Background
    onClose={props.closeOnBackgroundClick ? props.onClose : undefined}
  >
    <Container onClose={props.onClose} title={props.title}>
      {props.children}
    </Container>
  </Background>
)

type BackgroundProps = {
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
    <div className={b("background")} onClick={props.onClose}>
      {props.children}
    </div>
  )
}

type ContainerProps = ModalProps

const Container = (props: ContainerProps) => (
  <div
    className={b("container", { closeable: !!props.onClose })}
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
