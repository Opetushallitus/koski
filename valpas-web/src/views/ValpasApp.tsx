import React from "react"
import { Page } from "../components/containers/Page"
import { MainNavigation } from "../components/navigation/MainNavigation"
import { t } from "../i18n/i18n"
import { PerusopetusView } from "./hakutilanne/PerusopetusView"

const navOptions = [
  {
    key: "hakutilanne",
    display: t("ylänavi__hakutilanne"),
  },
]

export default () => {
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
