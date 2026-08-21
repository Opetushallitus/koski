import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import React from "react"
import { createRoot } from "react-dom/client"
import "regenerator-runtime/runtime"
import { fetchAppConfiguration } from "./api/api"
import { withRetries } from "./api/apiUtils"
import { getLanguage } from "./i18n/i18n"
import { enableFeature } from "./state/featureFlags"
import "./style/index.less"
import { ValpasApp } from "./views/ValpasApp"
import "./window.ts"

// Hack: Pakotetaan Parcel pitämään nämä mukana bundlessa
// Kts. https://github.com/date-fns/date-fns/issues/3670#issuecomment-1899246376
//      https://github.com/parcel-bundler/parcel/issues/9676
import { formatters, longFormatters } from "date-fns"
const FORCE_BUNDLE = [formatters, longFormatters]

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
  await loadWindowProperties()
  // Kieli luetaan vasta konfiguraation haun jälkeen: haku täydentää puuttuvan lang-evästeen käyttäjän
  // asiointikielestä, joten aiemmin luettuna attribuutti jäisi ensimmäisellä sivulatauksella suomeksi,
  // vaikka käyttöliittymä renderöityisi oikealla kielellä.
  document.documentElement.lang = getLanguage()
  const root = createRoot(document.getElementById("app")!)
  root.render(<ValpasApp />)
}

main()

window.enableFeature = enableFeature
