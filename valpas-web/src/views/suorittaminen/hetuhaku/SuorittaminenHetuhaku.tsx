import React from "react"
import {
  fetchHenkilöhakuSuorittaminen,
  fetchHenkilöhakuSuorittaminenCache,
} from "../../../api/api"
import { useApiMethod } from "../../../api/apiHooks"
import { Page } from "../../../components/containers/Page"
import { T } from "../../../i18n/i18n"
import { createSuorittaminenHetuhakuPath } from "../../../state/paths"
import { OppijaSearch } from "../../../views/oppijasearch/OppijaSearch"
import { SuorittaminenNavigation } from "../SuorittaminenNavigation"

export const SuorittaminenHetuhaku = () => {
  const search = useApiMethod(
    fetchHenkilöhakuSuorittaminen,
    fetchHenkilöhakuSuorittaminenCache
  )

  return (
    <Page id="suorittaminenhetuhaku">
      <SuorittaminenNavigation />
      <p>
        <T id={"suorittaminenhaku__ohje"} />
      </p>
      <OppijaSearch
        searchState={search}
        onQuery={search.call}
        prevPath={createSuorittaminenHetuhakuPath()}
        eiLöytynytIlmoitusId={"oppijahaku__ei_tuloksia"}
        error403Id={"oppijahaku__ei_tuloksia"}
      />
    </Page>
  )
}
