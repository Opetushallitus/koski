import React from "react"
import {
  fetchHenkilöhakuSuorittaminen,
  fetchHenkilöhakuSuorittaminenCache,
} from "../../../api/api"
import { useApiMethod } from "../../../api/apiHooks"
import { Page } from "../../../components/containers/Page"
import { DummyOrganisaatioValitsin } from "../../../components/shared/OrganisaatioValitsin"
import { t, T } from "../../../i18n/i18n"
import { withRequiresSuorittamisenValvonta } from "../../../state/accessRights"
import { suorittaminenHetuhakuPath } from "../../../state/paths"
import { OppijaSearch } from "../../../views/oppijasearch/OppijaSearch"
import { SuorittaminenNavigation } from "../SuorittaminenNavigation"

export const SuorittaminenHetuhaku = withRequiresSuorittamisenValvonta(() => {
  const search = useApiMethod(
    fetchHenkilöhakuSuorittaminen,
    fetchHenkilöhakuSuorittaminenCache,
  )

  return (
    <Page id="suorittaminenhetuhaku">
      <DummyOrganisaatioValitsin
        label={t("Oppilaitos")}
        placeholderText={t("oppijahaku__organisaatiovalitsin_ei_käytössä")}
      />
      <SuorittaminenNavigation />
      <p>
        <T id={"suorittaminenhaku__ohje"} />
      </p>
      <OppijaSearch
        searchState={search}
        onQuery={search.call}
        prevPath={suorittaminenHetuhakuPath.href()}
      />
    </Page>
  )
})
