import bem from "bem-ts"
import copy from "copy-to-clipboard"
import React, { useCallback, useState } from "react"
import { T } from "../i18n/i18n"
import { joinClassNames } from "../utils/classnames"
import { SuccessIcon } from "./icons/Icon"
import "./Password.less"

const b = bem("password")

export type PasswordProps = {
  children: string
  className?: string
}

export const Password = (props: PasswordProps) => {
  const [copied, setCopied] = useState(false)
  const copyToClipboard = useCallback(() => {
    copy(props.children)
    setCopied(true)
  }, [props.children])

  return (
    <div className={joinClassNames(b("container"), props.className)}>
      <span className={b("label")}>
        <T id="rouhinta_tiedoston_salasana" />:
      </span>
      <span className={b("value")}>{props.children}</span>
      <span className={b("copy")} onClick={copyToClipboard}>
        <T id="btn_kopioi_leikepöydälle" />
      </span>
      {copied && <SuccessIcon className={b("ok")} inline />}
    </div>
  )
}
