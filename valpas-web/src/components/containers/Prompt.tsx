import React, { useMemo, useState } from "react"
import { T } from "../../i18n/i18n"
import { ButtonGroup } from "../buttons/ButtonGroup"
import { RaisedButton } from "../buttons/RaisedButton"
import { Modal } from "./Modal"

export type PromptProps = {
  children: React.ReactNode
  onConfirm: () => void
  onReject: () => void
}

export const Prompt = (props: PromptProps) => (
  <Modal onClose={props.onReject} title={props.children}>
    <ButtonGroup>
      <RaisedButton onClick={props.onConfirm} testId="confirm-yes">
        <T id="Kyllä" />
      </RaisedButton>
      <RaisedButton onClick={props.onReject} testId="confirm-no">
        <T id="Ei" />
      </RaisedButton>
    </ButtonGroup>
  </Modal>
)

export type UsePrompt = {
  component: React.ReactNode | null
  show: (prompt: string, onConfirm: () => void) => void
  hide: () => void
}

export const usePrompt = (): UsePrompt => {
  type PromptState = {
    text: string
    onConfirm: () => void
  }

  const [prompt, setPrompt] = useState<PromptState | null>(null)

  return useMemo(
    () => ({
      component: prompt && (
        <Prompt onConfirm={prompt.onConfirm} onReject={() => setPrompt(null)}>
          {prompt.text}
        </Prompt>
      ),
      show: (text, onConfirm) => {
        setPrompt({ text, onConfirm })
      },
      hide: () => {
        setPrompt(null)
      },
    }),
    [prompt],
  )
}
