import React from 'react'
import { useEffect } from 'react'
import { useSafeState } from '../api-fetch'

export type ModalState = {
  // True jos modaali on päällimmäisenä
  isActive: boolean
  // Propertyt jotka pitää antaa modaalin sisällön omistavalle containerille
  props: ModalProps
}

export type ModalProps = {
  'aria-hidden': boolean
  style?: React.CSSProperties
}

/**
 * Rekisteröi kutsuvan komponentin modaaliksi ja palauttaa modaalille aktiivisuustiedot.
 * Sinun ei yleensä tarvitse käyttää tätä hookia, vaan voit käyttää geneeristä Modal-komponenttia.
 *
 * @returns ModalState
 */
export const useModalState = (): ModalState => {
  const [state, setState] = useSafeState(inactiveModalState())
  useEffect(() => modalManager.register(setState), [setState])
  return state
}

// Context provider

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

    setTimeout(() => {
      document.body.setAttribute(
        'aria-hidden',
        this.modalStack.length > 0 ? 'true' : 'false'
      )
    }, 300)
  }
}

const modalManager = new ModalManager()
