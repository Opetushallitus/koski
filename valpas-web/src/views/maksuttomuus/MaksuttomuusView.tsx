import React from "react"
import { createMaksuttomuusPath } from "../../state/paths"
import {
  fetchHenkilöhakuMaksuttomuus,
  fetchHenkilöhakuMaksuttomuusCache,
} from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { Page } from "../../components/containers/Page"
import { OppijaSearch } from "../oppijasearch/OppijaSearch"

export const MaksuttomuusView = () => (
  <Page id="maksuttomuus">
    <OppijaSearch
      searchApiMethod={useApiMethod(
        fetchHenkilöhakuMaksuttomuus,
        fetchHenkilöhakuMaksuttomuusCache
      )}
      prevPath={createMaksuttomuusPath()}
      eiLöytynytIlmoitusId={"oppijahaku__maksuttomuutta_ei_pysty_päättelemään"}
      error403Id={"oppijahaku__maksuttomuus_ei_näytettävä_oppija"}
    />
  </Page>
)
