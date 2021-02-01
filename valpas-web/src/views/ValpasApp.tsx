import React from "react"
import { Page } from "../components/containers/Page"
import { MainNavigation } from "../components/navigation/MainNavigation"
import { t } from "../i18n/i18n"
import { redirectToLoginReturnUrl } from "../state/auth"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"

const navOptions = [
  {
    key: "hakutilanne",
    display: t("ylänavi__hakutilanne"),
  },
]

export default () => {
  if (redirectToLoginReturnUrl()) {
    return null
  }

  return (
    <Page id="valpas-app">
      <MainNavigation
        selected="hakutilanne"
        options={navOptions}
        onChange={() => null}
      />
      <PerusopetusView />
    </Page>
  )
}
