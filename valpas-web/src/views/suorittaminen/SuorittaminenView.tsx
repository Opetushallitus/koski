import React from "react"
import { createSuorittaminenPath } from "~state/paths"
import {
  fetchHenkilöhakuSuorittaminen,
  fetchHenkilöhakuSuorittaminenCache,
} from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { Page } from "../../components/containers/Page"
import { T } from "../../i18n/i18n"
import { OppijaSearch } from "../oppijasearch/OppijaSearch"

export const SuorittaminenView = () => (
  <Page id="suorittaminen">
    <p>
      <T id={"oppijahaku__suorittaminen_väliaikainen_viesti"} />
    </p>
    <OppijaSearch
      searchApiMethod={useApiMethod(
        fetchHenkilöhakuSuorittaminen,
        fetchHenkilöhakuSuorittaminenCache
      )}
      prevPath={createSuorittaminenPath()}
      eiLöytynytIlmoitusId={"oppijahaku__ei_tuloksia"}
      error403Id={"oppijahaku__ei_tuloksia"}
    />
  </Page>
)
