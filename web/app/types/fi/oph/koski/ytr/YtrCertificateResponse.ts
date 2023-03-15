import {
  YtrCertificateBlocked,
  isYtrCertificateBlocked
} from './YtrCertificateBlocked'
import {
  YtrCertificateCompleted,
  isYtrCertificateCompleted
} from './YtrCertificateCompleted'
import {
  YtrCertificateInProgress,
  isYtrCertificateInProgress
} from './YtrCertificateInProgress'
import {
  YtrCertificateInternalError,
  isYtrCertificateInternalError
} from './YtrCertificateInternalError'
import {
  YtrCertificateNotStarted,
  isYtrCertificateNotStarted
} from './YtrCertificateNotStarted'
import {
  YtrCertificateOldExamination,
  isYtrCertificateOldExamination
} from './YtrCertificateOldExamination'
import {
  YtrCertificateServiceUnavailable,
  isYtrCertificateServiceUnavailable
} from './YtrCertificateServiceUnavailable'
import {
  YtrCertificateTimeout,
  isYtrCertificateTimeout
} from './YtrCertificateTimeout'

/**
 * YtrCertificateResponse
 *
 * @see `fi.oph.koski.ytr.YtrCertificateResponse`
 */
export type YtrCertificateResponse =
  | YtrCertificateBlocked
  | YtrCertificateCompleted
  | YtrCertificateInProgress
  | YtrCertificateInternalError
  | YtrCertificateNotStarted
  | YtrCertificateOldExamination
  | YtrCertificateServiceUnavailable
  | YtrCertificateTimeout

export const isYtrCertificateResponse = (a: any): a is YtrCertificateResponse =>
  isYtrCertificateBlocked(a) ||
  isYtrCertificateCompleted(a) ||
  isYtrCertificateInProgress(a) ||
  isYtrCertificateInternalError(a) ||
  isYtrCertificateNotStarted(a) ||
  isYtrCertificateOldExamination(a) ||
  isYtrCertificateServiceUnavailable(a) ||
  isYtrCertificateTimeout(a)
