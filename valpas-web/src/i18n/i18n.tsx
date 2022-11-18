import Cookie from "js-cookie"
import React, { useMemo } from "react"
import {
  supportedLanguages,
  Translation,
  TranslationId,
} from "../state/apitypes/appConfiguration"
import { KoodistoKoodiviite } from "../state/apitypes/koodistot"
import { Language, LocalizedString } from "../state/common"
import { logWarning } from "../utils/log"

type ParamsMap = Record<string, string | number>

const getString = (id: TranslationId) =>
  (window.valpasLocalizationMap || {})[id]
const missing: Record<string, boolean> = {}
let logMissingTranslationWarnings = true

export const getLanguage = (): Language => {
  const maybeLanguage: unknown = Cookie.get("lang")
  const language = supportedLanguages.find(
    (validLanguage) => validLanguage === maybeLanguage
  )

  return language || "fi"
}

export const setLanguage = (newLang: Language) => {
  Cookie.set("lang", newLang)
  window.location.reload()
}

export const t = (id: TranslationId, params?: ParamsMap): Translation => {
  if (!id) {
    return ""
  }
  console.debug(`_vlocalize: ${id}`)

  const tRecursive = (
    usedLanguage: Language,
    id: TranslationId,
    params?: ParamsMap
  ): Translation => {
    const localizedString = getString(id)

    if (!localizedString || !localizedString[usedLanguage]) {
      if (!missing[usedLanguage + "." + id] && logMissingTranslationWarnings) {
        logWarning(`Käännös puuttuu ${usedLanguage}:`, id)
        missing[usedLanguage + "." + id] = true
      }

      return usedLanguage === "fi" ? id : tRecursive("fi", id, params)
    }

    const source = localizedString[usedLanguage]
    return params ? replaceParams(source, params) : source
  }

  return tRecursive(getLanguage(), id, params)
}

export const tParagraphs = (
  id: TranslationId,
  params?: ParamsMap
): Translation[] =>
  t(id, params)
    .split("\n")
    .map((s) => s.trim())
    .filter((s) => s.length > 0)

const replaceParams = (source: string, params: ParamsMap): string =>
  Object.entries(params).reduce(
    (str, [key, value]) => str.replace(`{{${key}}}`, value.toString()),
    source
  )

export type LocalizedTextProps = {
  id: TranslationId
  params?: ParamsMap
}

export const T = (props: LocalizedTextProps) => <>{t(props.id, props.params)}</>

export const TParagraphs = (props: LocalizedTextProps) => (
  <>
    {tParagraphs(props.id, props.params).map((text, index) => (
      <p key={index}>{text}</p>
    ))}
  </>
)

export const getLocalized = (localizedString: LocalizedString): string =>
  localizedString[getLanguage()] ||
  localizedString["fi"] ||
  localizedString["sv"] ||
  localizedString["en"] ||
  "KÄÄNNÖS PUUTTUU"

export const getLocalizedMaybe = (
  localizedString?: LocalizedString
): string | undefined =>
  localizedString === undefined ? undefined : getLocalized(localizedString)

export const formatFixedNumber = (
  n: number | undefined,
  fractionDigits: number
): string | undefined => n?.toFixed(fractionDigits).replace(".", ",")

export const useLanguage = () => useMemo(() => getLanguage(), [])

export const disableMissingTranslationWarnings = () => {
  logMissingTranslationWarnings = false
}

const zeroWidthSpace = "\u200B"
const wrappableSpecialCharRegex = /(\/)/
export const makeAutoWrappable = (s: string): string =>
  s.split(wrappableSpecialCharRegex).join(zeroWidthSpace)

/**
 * Palauttaa koodiviitteestä aina edes jonkilaisen esitettävän merkkijonon
 */
export const koodiviiteToShortString = (
  koodiviite: KoodistoKoodiviite
): string =>
  getLocalizedMaybe(koodiviite.lyhytNimi) ||
  getLocalizedMaybe(koodiviite.nimi) ||
  koodiviite.koodiarvo
