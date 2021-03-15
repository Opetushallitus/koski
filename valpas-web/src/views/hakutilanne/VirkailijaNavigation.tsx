import React from "react"
import { MainNavigation } from "../../components/navigation/MainNavigation"
import { t } from "../../i18n/i18n"

export const VirkailijaNavigation = () => {
  const navOptions = [
    {
      key: "hakutilanne",
      display: t("ylänavi__hakutilanne"),
    },
  ]

  return (
    <MainNavigation
      selected="hakutilanne"
      options={navOptions}
      onChange={() => null}
    />
  )
}
