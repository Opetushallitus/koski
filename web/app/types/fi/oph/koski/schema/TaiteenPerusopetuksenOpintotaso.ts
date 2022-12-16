import {
  ArkkitehtuurinOpintotaso,
  isArkkitehtuurinOpintotaso
} from './ArkkitehtuurinOpintotaso'
import {
  KuvataiteenOpintotaso,
  isKuvataiteenOpintotaso
} from './KuvataiteenOpintotaso'
import { KäsityönOpintotaso, isKäsityönOpintotaso } from './KasityonOpintotaso'
import {
  MediataiteenOpintotaso,
  isMediataiteenOpintotaso
} from './MediataiteenOpintotaso'
import { MusiikinOpintotaso, isMusiikinOpintotaso } from './MusiikinOpintotaso'
import {
  SanataiteenOpintotaso,
  isSanataiteenOpintotaso
} from './SanataiteenOpintotaso'
import {
  SirkustaiteenOpintotaso,
  isSirkustaiteenOpintotaso
} from './SirkustaiteenOpintotaso'
import { TanssinOpintotaso, isTanssinOpintotaso } from './TanssinOpintotaso'
import {
  TeatteritaiteenOpintotaso,
  isTeatteritaiteenOpintotaso
} from './TeatteritaiteenOpintotaso'

/**
 * TaiteenPerusopetuksenOpintotaso
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOpintotaso`
 */
export type TaiteenPerusopetuksenOpintotaso =
  | ArkkitehtuurinOpintotaso
  | KuvataiteenOpintotaso
  | KäsityönOpintotaso
  | MediataiteenOpintotaso
  | MusiikinOpintotaso
  | SanataiteenOpintotaso
  | SirkustaiteenOpintotaso
  | TanssinOpintotaso
  | TeatteritaiteenOpintotaso

export const isTaiteenPerusopetuksenOpintotaso = (
  a: any
): a is TaiteenPerusopetuksenOpintotaso =>
  isArkkitehtuurinOpintotaso(a) ||
  isKuvataiteenOpintotaso(a) ||
  isKäsityönOpintotaso(a) ||
  isMediataiteenOpintotaso(a) ||
  isMusiikinOpintotaso(a) ||
  isSanataiteenOpintotaso(a) ||
  isSirkustaiteenOpintotaso(a) ||
  isTanssinOpintotaso(a) ||
  isTeatteritaiteenOpintotaso(a)
