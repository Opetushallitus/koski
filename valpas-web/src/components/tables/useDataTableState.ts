import {
  nonStoredState,
  sessionStateStorage,
  useStoredState,
} from "../../state/useSessionStoreState"
import { createFilter, DataFilter, FilterFn } from "./DataTableFilter"
import { TableCellSize } from "./Table"

export type DataTableState = {
  filters: FilterState
  sort: SortState
}

type SerializedDataTableState = {
  filters: Array<string | null>
  sort: SortState
}

export type FilterState = Array<{ fn: FilterFn | null; value: string | null }>

export type SortState = {
  columnIndex: number
  ascending: boolean
}

export type Column = {
  label: string
  tooltip?: string
  filter?: DataFilter
  size?: TableCellSize
  indicatorSpace?: true | false | "auto"
}

// Kokonaistila

const initialState = (columnCount: number): DataTableState => ({
  filters: initialFilterState(columnCount),
  sort: initialSortState(),
})

const createStorage = (storageName: string, columns: Column[]) =>
  sessionStateStorage<DataTableState, SerializedDataTableState>(
    `${storageName}-2.0-${columns.map((c) => c.filter?.slice(0, 2)).join(".")}`,
    initialState(columns.length),
    (state) => ({ ...state, filters: state.filters.map((f) => f.value) }),
    (serialized) => ({
      ...serialized,
      filters: serialized.filters.map((value, index) => ({
        value,
        fn: createFilter(columns[index]?.filter, value),
      })),
    })
  )

export const useDataTableState = (
  storageName: string | undefined,
  columns: Column[]
) =>
  useStoredState<DataTableState>(
    storageName
      ? createStorage(storageName, columns)
      : nonStoredState(initialState(columns.length))
  )

// Filtterit

const initialFilterState = (length: number): FilterState =>
  new Array(length).fill({ value: null, fn: null })

export const setFilters =
  (filters: FilterState) =>
  (state: DataTableState): DataTableState => ({
    ...state,
    filters,
  })

// Järjestys

const initialSortState = (): SortState => ({
  columnIndex: 0,
  ascending: true,
})

export const sortByColumn =
  (columnIndex: number) =>
  (state: DataTableState): DataTableState => ({
    ...state,
    sort:
      columnIndex === state.sort.columnIndex
        ? { ...state.sort, ascending: !state.sort.ascending }
        : { columnIndex: columnIndex, ascending: true },
  })
