import * as A from 'fp-ts/Array'
import React, { useCallback, useContext, useMemo, useState } from 'react'
import * as E from 'fp-ts/Either'
import { pipe } from 'fp-ts/lib/function'
import { useEffect } from 'react'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  fetchOrganisaatioHierarkia,
  queryOrganisaatioHierarkia
} from '../util/koskiApi'
import { useDebounce } from '../util/useDebounce'

/**
 * Palauttaa käyttäjälle organisaatiohierarkian.
 *
 * @param queryText Optionaalinen sanahaku
 * @returns OrganisaatioHierarkia[]
 */
export const useOrganisaatioHierarkia = (
  queryText?: string
): OrganisaatioHierarkia[] => {
  const { load, query, queries } = useContext(OrganisaatioHierarkiaContext)

  useEffect(() => {
    load()
  }, [load])

  useDebounce(200, (text) => text && query(text), [queryText])

  return queries[asQueryKey(queryText)] || emptyResult
}

/**
 * Palauttaa true, jos käyttäjälle palautuu yksikin organisaatio sanahaun ollessa tyhjä.
 *
 * @returns boolean
 */
export const useHasOwnOrganisaatiot = (): boolean => {
  const { load, queries } = useContext(OrganisaatioHierarkiaContext)

  useEffect(() => {
    load()
  }, [load])

  return !!queries[ROOT_QUERY] && A.isNonEmpty(queries[ROOT_QUERY])
}

// Context provider

const ROOT_QUERY = ''

export type OrganisaatioHierarkiaContext = {
  load: () => Promise<void>
  query: (searchText: string) => Promise<void>
  queries: Record<string, OrganisaatioHierarkia[]>
}

class OrganisaatioHierarkiaLoader {
  cache: Record<string, OrganisaatioHierarkia[]> = {}

  async query(queryText?: string): Promise<void> {
    const key = asQueryKey(queryText)
    const cached = this.cache[key]
    if (!cached) {
      this.cache[key] = []
      pipe(
        await (key
          ? queryOrganisaatioHierarkia(key)
          : fetchOrganisaatioHierarkia()),
        E.map((response) => {
          this.cache[key] = response.data
        })
      )
    }
  }
}

const asQueryKey = (queryText?: string) =>
  queryText?.trim().toLowerCase() || ROOT_QUERY

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
    await organisaatioLoader.query(ROOT_QUERY)
    setQueries({
      ...organisaatioLoader.cache
    })
  }, [])

  const query = useCallback(async (searchText: string) => {
    await organisaatioLoader.query(searchText)
    setQueries({ ...organisaatioLoader.cache })
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

const emptyResult: OrganisaatioHierarkia[] = []
