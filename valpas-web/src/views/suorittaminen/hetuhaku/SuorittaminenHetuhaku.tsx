import React from "react"
import {
  fetchHenkilöhakuSuorittaminen,
  fetchHenkilöhakuSuorittaminenCache,
} from "../../../api/api"
import { useApiMethod } from "../../../api/apiHooks"
import { Page } from "../../../components/containers/Page"
import { T } from "../../../i18n/i18n"
import { isFeatureFlagEnabled } from "../../../state/featureFlags"
import { createSuorittaminenPath } from "../../../state/paths"
import { OppijaSearch } from "../../../views/oppijasearch/OppijaSearch"
import { SuorittaminenNavigation } from "../SuorittaminenNavigation"

export const SuorittaminenHetuhaku = () => {
  const search = useApiMethod(
    fetchHenkilöhakuSuorittaminen,
    fetchHenkilöhakuSuorittaminenCache
  )

  return (
    <Page id="suorittaminenhetuhaku">
      {isFeatureFlagEnabled("suorittamisenvalvontalista") ? (
        <>
          <SuorittaminenNavigation />
          <p>
            <T id={"suorittaminenhaku__ohje"} />
          </p>
        </>
      ) : (
        <p>
          <T id={"oppijahaku__suorittaminen_väliaikainen_viesti"} />
        </p>
      )}
      <OppijaSearch
        searchState={search}
        onQuery={search.call}
        prevPath={createSuorittaminenPath()}
        eiLöytynytIlmoitusId={"oppijahaku__ei_tuloksia"}
        error403Id={"oppijahaku__ei_tuloksia"}
      />
    </Page>
  )
}
