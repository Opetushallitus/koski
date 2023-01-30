import React, { useCallback, useContext, useMemo, useState } from 'react'
import * as E from 'fp-ts/Either'
import { constant, identity, pipe } from 'fp-ts/lib/function'
import { useEffect } from 'react'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  fetchOrganisaatioHierarkia,
  queryOrganisaatioHierarkia
} from '../util/koskiApi'

const ROOT_QUERY = ''

export type OrganisaatioHierarkiaContext = {
  load: () => void
  query: (searchText: string) => void
  queries: Record<string, OrganisaatioHierarkia[]>
}

class OrganisaatioHierarkiaLoader {
  cache: Record<string, OrganisaatioHierarkia[]> = {}

  async query(queryText?: string): Promise<boolean> {
    const needle = queryText?.trim() || ROOT_QUERY
    const cached = this.cache[needle]
    if (cached) {
      return false
    } else {
      this.cache[needle] = []
      return pipe(
        await (needle
          ? queryOrganisaatioHierarkia(needle)
          : fetchOrganisaatioHierarkia()),
        E.map((response) => {
          this.cache[needle] = response.data
          return response.data
        }),
        E.fold(constant(false), constant(true))
      )
    }
  }
}

const organisaatioLoader = new OrganisaatioHierarkiaLoader()

const providerMissing = () => {
  throw new Error('OrganisaatioHierarkiaProvider is missing')
}

const OrganisaatioHierarkiaContext =
  React.createContext<OrganisaatioHierarkiaContext>({
    load: providerMissing,
    query: providerMissing,
    queries: {}
  })

export const OrganisaatioHierarkiaProvider: React.FC<
  React.PropsWithChildren
> = (props) => {
  const [queries, setQueries] = useState<
    Record<string, OrganisaatioHierarkia[]>
  >({})

  const load = useCallback(async () => {
    if (await organisaatioLoader.query(ROOT_QUERY)) {
      setQueries(organisaatioLoader.cache)
    }
  }, [])

  const query = useCallback(async (searchText: string) => {
    if (await organisaatioLoader.query(searchText)) {
      setQueries(organisaatioLoader.cache)
    }
  }, [])

  const contextValue = useMemo(
    () => ({
      queries,
      load,
      query
    }),
    [load, queries, query]
  )

  return (
    <OrganisaatioHierarkiaContext.Provider value={contextValue}>
      {props.children}
    </OrganisaatioHierarkiaContext.Provider>
  )
}

export const useOrganisaatioHierarkia = (
  queryText?: string
): OrganisaatioHierarkia[] => {
  const { load, query, queries } = useContext(OrganisaatioHierarkiaContext)

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    if (queryText) {
      query(queryText)
    }
  }, [query, queryText])

  return queries[queryText || ROOT_QUERY] || emptyResult
}

const emptyResult: OrganisaatioHierarkia[] = []
