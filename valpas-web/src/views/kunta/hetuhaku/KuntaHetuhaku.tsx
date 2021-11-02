import React from "react"
import {
  fetchHenkilöhakuKunta,
  fetchHenkilöhakuKuntaCache,
} from "../../../api/api"
import { useApiMethod } from "../../../api/apiHooks"
import { Page } from "../../../components/containers/Page"
import { DummyOrganisaatioValitsin } from "../../../components/shared/OrganisaatioValitsin"
import { t } from "../../../i18n/i18n"
import { withRequiresKuntavalvonta } from "../../../state/accessRights"
import { isFeatureFlagEnabled } from "../../../state/featureFlags"
import { kunnanHetuhakuPath } from "../../../state/paths"
import { OppijaSearch } from "../../../views/oppijasearch/OppijaSearch"
import { KuntaNavigation } from "../KuntaNavigation"
import { KuntaHetulistahaku } from "./KuntaHetulistahaku"

export const KuntaHetuhaku = withRequiresKuntavalvonta(() => {
  const search = useApiMethod(fetchHenkilöhakuKunta, fetchHenkilöhakuKuntaCache)

  return (
    <Page id="kuntahetuhaku">
      <DummyOrganisaatioValitsin
        label={t("Kunta")}
        placeholderText={t("oppijahaku__organisaatiovalitsin_ei_käytössä")}
      />
      <KuntaNavigation />
      <OppijaSearch
        searchState={search}
        onQuery={search.call}
        prevPath={kunnanHetuhakuPath.href()}
      />
      {isFeatureFlagEnabled("rouhinta") && <KuntaHetulistahaku />}
    </Page>
  )
})
