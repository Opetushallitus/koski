import React from "react"
import { t } from "../../i18n/i18n"
import { ErrorView } from "../ErrorView"

export const KansalainenLoginErrorView = () => (
  <ErrorView
    title={t("kirjautuminen_epäonnistui")}
    message={t("kirjautuminen_epäonnistui_selite")}
  />
)

export const KansalainenEiTietojaOpintopolussaView = () => (
  <ErrorView
    title={t("tietoja_ei_opintopolussa")}
    message={t("tietoja_ei_opintopolussa_selite")}
  />
)
