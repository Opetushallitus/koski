import {
  AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine,
  isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine
} from './AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'
import {
  AikuistenPerusopetuksenPaikallinenOppiaine,
  isAikuistenPerusopetuksenPaikallinenOppiaine
} from './AikuistenPerusopetuksenPaikallinenOppiaine'
import { IBKurssi, isIBKurssi } from './IBKurssi'
import {
  LukionPaikallinenOpintojakso2019,
  isLukionPaikallinenOpintojakso2019
} from './LukionPaikallinenOpintojakso2019'
import {
  MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli,
  isMuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
} from './MuunKuinSaannellynKoulutuksenOsasuorituksenKoulutusmoduuli'
import {
  NuortenPerusopetuksenPaikallinenOppiaine,
  isNuortenPerusopetuksenPaikallinenOppiaine
} from './NuortenPerusopetuksenPaikallinenOppiaine'
import {
  OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
} from './OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import {
  Organisaatiohenkilö,
  isOrganisaatiohenkilö
} from './Organisaatiohenkilo'
import {
  PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi,
  isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
} from './PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'
import {
  PaikallinenAikuistenPerusopetuksenKurssi,
  isPaikallinenAikuistenPerusopetuksenKurssi
} from './PaikallinenAikuistenPerusopetuksenKurssi'
import {
  PaikallinenLukionKurssi2015,
  isPaikallinenLukionKurssi2015
} from './PaikallinenLukionKurssi2015'
import {
  PaikallinenLukionOppiaine2015,
  isPaikallinenLukionOppiaine2015
} from './PaikallinenLukionOppiaine2015'
import {
  PaikallinenLukionOppiaine2019,
  isPaikallinenLukionOppiaine2019
} from './PaikallinenLukionOppiaine2019'
import {
  PaikallinenLukioonValmistavanKoulutuksenKurssi,
  isPaikallinenLukioonValmistavanKoulutuksenKurssi
} from './PaikallinenLukioonValmistavanKoulutuksenKurssi'
import {
  PaikallinenLukioonValmistavanKoulutuksenOppiaine,
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine
} from './PaikallinenLukioonValmistavanKoulutuksenOppiaine'
import {
  PerusopetukseenValmistavanOpetuksenOppiaine,
  isPerusopetukseenValmistavanOpetuksenOppiaine
} from './PerusopetukseenValmistavanOpetuksenOppiaine'
import {
  TaiteenPerusopetuksenPaikallinenOpintokokonaisuus,
  isTaiteenPerusopetuksenPaikallinenOpintokokonaisuus
} from './TaiteenPerusopetuksenPaikallinenOpintokokonaisuus'
import {
  VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022,
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
} from './VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import {
  VapaanSivistystyönJotpaKoulutuksenOsasuoritus,
  isVapaanSivistystyönJotpaKoulutuksenOsasuoritus
} from './VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import {
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
} from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
} from './VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'

/**
 * StorablePreference
 *
 * @see `fi.oph.koski.schema.StorablePreference`
 */
export type StorablePreference =
  | AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | IBKurssi
  | LukionPaikallinenOpintojakso2019
  | MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
  | NuortenPerusopetuksenPaikallinenOppiaine
  | OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
  | Organisaatiohenkilö
  | PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
  | PaikallinenAikuistenPerusopetuksenKurssi
  | PaikallinenLukionKurssi2015
  | PaikallinenLukionOppiaine2015
  | PaikallinenLukionOppiaine2019
  | PaikallinenLukioonValmistavanKoulutuksenKurssi
  | PaikallinenLukioonValmistavanKoulutuksenOppiaine
  | PerusopetukseenValmistavanOpetuksenOppiaine
  | TaiteenPerusopetuksenPaikallinenOpintokokonaisuus
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
  | VapaanSivistystyönJotpaKoulutuksenOsasuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus

export const isStorablePreference = (a: any): a is StorablePreference =>
  isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isIBKurssi(a) ||
  isLukionPaikallinenOpintojakso2019(a) ||
  isMuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(a) ||
  isOrganisaatiohenkilö(a) ||
  isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi(a) ||
  isPaikallinenAikuistenPerusopetuksenKurssi(a) ||
  isPaikallinenLukionKurssi2015(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isPaikallinenLukionOppiaine2019(a) ||
  isPaikallinenLukioonValmistavanKoulutuksenKurssi(a) ||
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine(a) ||
  isPerusopetukseenValmistavanOpetuksenOppiaine(a) ||
  isTaiteenPerusopetuksenPaikallinenOpintokokonaisuus(a) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
    a
  ) ||
  isVapaanSivistystyönJotpaKoulutuksenOsasuoritus(a) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
    a
  ) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(a)
