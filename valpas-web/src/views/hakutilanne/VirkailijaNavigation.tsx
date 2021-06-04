import React from "react"
import { TabNavigation } from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"

export const VirkailijaNavigation = () => {
  const navOptions = [
    {
      key: "hakutilanne",
      display: t("hakeutumisvelvollisetnavi__hakutilanne"),
    },
  ]

  return (
    <TabNavigation
      selected="hakutilanne"
      options={navOptions}
      onChange={() => null}
    />
  )
}
