import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import React from "react"
import ReactDOM from "react-dom"
import "regenerator-runtime/runtime"
import { fetchAppConfiguration } from "./api/api"
import { withRetries } from "./api/apiUtils"
import { getLanguage } from "./i18n/i18n"
import { enableFeature } from "./state/featureFlags"
import "./style/index.less"
import { ValpasApp } from "./views/ValpasApp"

const loadWindowProperties = async (): Promise<void> =>
  pipe(
    await withRetries(3, fetchAppConfiguration),
    E.fold(
      (error) =>
        console.error(
          "Konfiguraation haku epäonnistui:",
          JSON.stringify(error),
        ),
      (props) => Object.assign(window, props.data),
    ),
  )

async function main() {
  document.documentElement.lang = getLanguage()
  await loadWindowProperties()
  ReactDOM.render(<ValpasApp />, document.getElementById("app"))
}

main()

window.enableFeature = enableFeature
