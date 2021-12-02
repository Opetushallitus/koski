import React from "react"
import { ErrorView } from "../ErrorView"

export const KansalainenLoginErrorView = () => (
  <ErrorView
    title="Kirjautuminen epäonnistui."
    message="Kirjautuminen epäonnistui teknisen virheen takia. Yritä uudestaan myöhemmin."
  />
)

export const KansalainenEiTietojaOpintopolussaView = () => (
  <ErrorView
    title="Tietojasi ei löydy Opintopolusta."
    message="Jos tietojesi kuuluisi näkyä palvelussa, voit ilmoittaa asiasta oppilaitoksellesi."
  />
)
