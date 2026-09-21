import {
  CompleteQueryResponse,
  isCompleteQueryResponse
} from './CompleteQueryResponse'
import {
  FailedQueryResponse,
  isFailedQueryResponse
} from './FailedQueryResponse'
import {
  PendingQueryResponse,
  isPendingQueryResponse
} from './PendingQueryResponse'
import {
  RunningQueryResponse,
  isRunningQueryResponse
} from './RunningQueryResponse'

/**
 * QueryResponse
 *
 * @see `fi.oph.koski.massaluovutus.QueryResponse`
 */
export type QueryResponse =
  | CompleteQueryResponse
  | FailedQueryResponse
  | PendingQueryResponse
  | RunningQueryResponse

export const isQueryResponse = (a: any): a is QueryResponse =>
  isCompleteQueryResponse(a) ||
  isFailedQueryResponse(a) ||
  isPendingQueryResponse(a) ||
  isRunningQueryResponse(a)
