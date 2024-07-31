import * as A from 'fp-ts/Array'
import React, { useCallback, useContext, useMemo, useState } from 'react'
import * as E from 'fp-ts/Either'
import { pipe } from 'fp-ts/lib/function'
import { useEffect } from 'react'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  OrgTypesToShow,
  fetchOrganisaatioHierarkia,
  queryOrganisaatioHierarkia
} from '../util/koskiApi'
import { useDebounce } from '../util/useDebounce'
import { nonNull } from '../util/fp/arrays'

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
 * Palauttaa käyttäjälle organisaatiohierarkian kaikista organisaatioista
 *
 * @param queryText Optionaalinen sanahaku
 * @returns OrganisaatioHierarkia[]
 */
export const useOrganisaatioHierarkiaSearch = (
  queryText?: string,
  orgTypesToShow?: OrgTypesToShow
): OrganisaatioHierarkia[] => {
  const { queryFromAll, searchQueries } = useContext(
    OrganisaatioHierarkiaContext
  )
  useDebounce(200, (text) => text && queryFromAll(text, orgTypesToShow), [
    queryText
  ])

  return searchQueries[asQueryKey(queryText, orgTypesToShow)] || emptyResult
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
  queryFromAll: (
    searchText: string,
    orgTypesToShow?: OrgTypesToShow
  ) => Promise<void>
  queries: Record<string, OrganisaatioHierarkia[]>
  searchQueries: Record<string, OrganisaatioHierarkia[]>
}

class OrganisaatioHierarkiaLoader {
  cache: Record<string, OrganisaatioHierarkia[]> = {}
  cacheAll: Record<string, OrganisaatioHierarkia[]> = {}

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

  async queryFromAll(
    queryText: string,
    orgTypesToShow?: OrgTypesToShow
  ): Promise<void> {
    const searchKey = asQueryKey(queryText)
    const cacheKey = asQueryKey(queryText, orgTypesToShow)

    const cached = this.cacheAll[cacheKey]
    if (!cached && searchKey.length >= 3) {
      this.cacheAll[cacheKey] = []
      pipe(
        await queryOrganisaatioHierarkia(
          searchKey,
          orgTypesToShow,
          orgTypesToShow === undefined
        ),
        E.map((response) => {
          this.cacheAll[cacheKey] = response.data
        })
      )
    }
  }
}

const queryKeyDelimiter = '///'

const asQueryKey = (queryText?: string, orgTypesToShow?: OrgTypesToShow) =>
  [queryText?.trim().toLowerCase() || ROOT_QUERY, orgTypesToShow]
    .filter(nonNull)
    .join(queryKeyDelimiter)

const organisaatioLoader = new OrganisaatioHierarkiaLoader()

const providerMissing = () => {
  throw new Error('OrganisaatioHierarkiaProvider is missing')
}

const OrganisaatioHierarkiaContext =
  React.createContext<OrganisaatioHierarkiaContext>({
    load: providerMissing,
    query: providerMissing,
    queryFromAll: providerMissing,
    queries: {},
    searchQueries: {}
  })

export const OrganisaatioHierarkiaProvider: React.FC<
  React.PropsWithChildren<{}>
> = (props) => {
  const [queries, setQueries] = useState<
    Record<string, OrganisaatioHierarkia[]>
  >({})
  const [searchQueries, setSearchQueries] = useState<
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

  const queryFromAll = useCallback(
    async (searchText: string, orgTypesToShow?: OrgTypesToShow) => {
      await organisaatioLoader.queryFromAll(searchText, orgTypesToShow)
      setSearchQueries({ ...organisaatioLoader.cacheAll })
    },
    []
  )

  const contextValue = useMemo(
    () => ({
      queries,
      searchQueries,
      load,
      query,
      queryFromAll
    }),
    [load, queries, query, queryFromAll, searchQueries]
  )

  return (
    <OrganisaatioHierarkiaContext.Provider value={contextValue}>
      {props.children}
    </OrganisaatioHierarkiaContext.Provider>
  )
}

const emptyResult: OrganisaatioHierarkia[] = []
