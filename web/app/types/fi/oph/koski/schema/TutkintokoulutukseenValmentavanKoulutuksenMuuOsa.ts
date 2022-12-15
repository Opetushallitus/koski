import {
  TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen,
  isTutkintokoulutukseenValmentavaPerustaitojenVahvistaminen
} from './TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen'
import {
  TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot,
  isTutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot
} from './TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot'
import {
  TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot,
  isTutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot
} from './TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot'
import {
  TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot,
  isTutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot
} from './TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot'
import {
  TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot,
  isTutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot
} from './TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot'
import {
  TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen,
  isTutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen
} from './TutkintokoulutukseenValmentavatTyoelamataidotJaTyopaikallaTapahtuvaOppiminen'

/**
 * TutkintokoulutukseenValmentavanKoulutuksenMuuOsa
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenMuuOsa`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenMuuOsa =
  | TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen
  | TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot
  | TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot
  | TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot
  | TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot
  | TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen

export const isTutkintokoulutukseenValmentavanKoulutuksenMuuOsa = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenMuuOsa =>
  isTutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(a) ||
  isTutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(a) ||
  isTutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
    a
  ) ||
  isTutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(a) ||
  isTutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(a) ||
  isTutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
    a
  )
