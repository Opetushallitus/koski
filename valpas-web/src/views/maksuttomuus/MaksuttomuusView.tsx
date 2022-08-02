import React from "react"
import {
  fetchHenkilöhakuMaksuttomuus,
  fetchHenkilöhakuMaksuttomuusCache,
} from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { Page } from "../../components/containers/Page"
import { Aikaleima } from "../../components/shared/Aikaleima"
import { withRequiresMaksuttomuudenValvonta } from "../../state/accessRights"
import { maksuttomuusPath } from "../../state/paths"
import { MaksuttomuusOppijaSearch } from "./MaksuttomuusOppijaSearch"

export const MaksuttomuusView = withRequiresMaksuttomuudenValvonta(() => {
  const search = useApiMethod(
    fetchHenkilöhakuMaksuttomuus,
    fetchHenkilöhakuMaksuttomuusCache
  )

  return (
    <Page id="maksuttomuus">
      <Aikaleima />
      <MaksuttomuusOppijaSearch
        searchState={search}
        onQuery={search.call}
        prevPath={maksuttomuusPath.href()}
      />
    </Page>
  )
})
