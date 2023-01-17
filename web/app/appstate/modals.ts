import { useEffect } from 'react'
import { useSafeState } from '../api-fetch'

export type ModalState = {
  isActive: boolean
  props: ModalProps
}

export type ModalProps = {
  'aria-hidden': boolean
  style?: React.CSSProperties
}

export type ModalStateListener = (state: ModalState) => void
export type Destructor = () => void

const inactiveModalState = (zIndex?: number): ModalState => ({
  isActive: false,
  props: {
    'aria-hidden': true,
    style:
      zIndex !== undefined
        ? {
            zIndex: 1000 + zIndex
          }
        : undefined
  }
})

const activeModalState = (zIndex: number): ModalState => ({
  isActive: true,
  props: {
    'aria-hidden': false,
    style: { zIndex: 1000 + zIndex }
  }
})

class ModalManager {
  modalStack: ModalStateListener[] = []

  register(listener: ModalStateListener): Destructor {
    this.modalStack.push(listener)
    this.emitState()

    return () => {
      this.modalStack = this.modalStack.filter((m) => m !== listener)
      this.emitState()
    }
  }

  emitState() {
    const lastIndex = this.modalStack.length - 1
    this.modalStack.forEach((setState, index) => {
      if (index === lastIndex) {
        setState(activeModalState(index))
      } else {
        setState(inactiveModalState(index))
      }
    })

    document.body.setAttribute(
      'aria-hidden',
      this.modalStack.length > 0 ? 'true' : 'false'
    )
  }
}

const modalManager = new ModalManager()

export const useModalState = (): ModalState => {
  const [state, setState] = useSafeState(inactiveModalState())
  useEffect(() => modalManager.register(setState), [])
  return state
}
