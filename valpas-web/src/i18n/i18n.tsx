import React from "react"
import { logWarning } from "../utils/log"

export type TranslationId = string
export type Translation = string

export const t = (id: TranslationId): Translation => {
  const translation = mockTranslations[id]
  if (!translation) {
    logWarning(`Käännös puuttuu: "${id}"`)
  }
  return translation || id
}

export type LocalizedTextProps = {
  id: TranslationId
}

export const T = (props: LocalizedTextProps) => <>{t(props.id)}</>

// TODO: Fetch these from API endpoint on init

const mockTranslations: Record<string, Translation> = {
  login__otsikko: "Kirjautuminen",
  login__kayttaja: "Käyttäjätunnus",
  login__salasana: "Salasana",
  login__btn_kirjaudu: "Kirjaudu",
}
