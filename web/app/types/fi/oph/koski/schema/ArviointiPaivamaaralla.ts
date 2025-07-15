import {
  AmmatillinenArviointi,
  isAmmatillinenArviointi
} from './AmmatillinenArviointi'
import { IBKurssinArviointi, isIBKurssinArviointi } from './IBKurssinArviointi'
import {
  KorkeakoulunKoodistostaLöytyväArviointi,
  isKorkeakoulunKoodistostaLöytyväArviointi
} from './KorkeakoulunKoodistostaLoytyvaArviointi'
import {
  KorkeakoulunPaikallinenArviointi,
  isKorkeakoulunPaikallinenArviointi
} from './KorkeakoulunPaikallinenArviointi'
import {
  LukiodiplominSuoritusJaArviointi,
  isLukiodiplominSuoritusJaArviointi
} from './LukiodiplominSuoritusJaArviointi'
import {
  LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi,
  isLukionOmanÄidinkielenOpinnonOsasuorituksenArviointi
} from './LukionOmanAidinkielenOpinnonOsasuorituksenArviointi'
import {
  LukutaitokoulutuksenArviointi,
  isLukutaitokoulutuksenArviointi
} from './LukutaitokoulutuksenArviointi'
import {
  MuunAmmatillisenKoulutuksenArviointi,
  isMuunAmmatillisenKoulutuksenArviointi
} from './MuunAmmatillisenKoulutuksenArviointi'
import {
  NumeerinenLukionArviointi,
  isNumeerinenLukionArviointi
} from './NumeerinenLukionArviointi'
import {
  NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019,
  isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
} from './NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import { NäytönArviointi, isNäytönArviointi } from './NaytonArviointi'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi,
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi
} from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { PuhviKoe2019, isPuhviKoe2019 } from './PuhviKoe2019'
import {
  SanallinenLukionArviointi,
  isSanallinenLukionArviointi
} from './SanallinenLukionArviointi'
import {
  SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019,
  isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
} from './SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
import {
  SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi,
  isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi
} from './SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
import {
  SuullisenKielitaidonKoe2019,
  isSuullisenKielitaidonKoe2019
} from './SuullisenKielitaidonKoe2019'
import {
  TaiteenPerusopetuksenArviointi,
  isTaiteenPerusopetuksenArviointi
} from './TaiteenPerusopetuksenArviointi'
import {
  TelmaJaValmaArviointi,
  isTelmaJaValmaArviointi
} from './TelmaJaValmaArviointi'
import {
  VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022,
  isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022
} from './VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import {
  ValtionhallinnonKielitutkinnonArviointi,
  isValtionhallinnonKielitutkinnonArviointi
} from './ValtionhallinnonKielitutkinnonArviointi'
import {
  VapaanSivistystyöJotpaKoulutuksenArviointi,
  isVapaanSivistystyöJotpaKoulutuksenArviointi
} from './VapaanSivistystyoJotpaKoulutuksenArviointi'
import {
  VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi,
  isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi
} from './VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import {
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi,
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi
} from './VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import {
  VapaanSivistystyönOsaamismerkinArviointi,
  isVapaanSivistystyönOsaamismerkinArviointi
} from './VapaanSivistystyonOsaamismerkinArviointi'
import {
  YleisenKielitutkinnonOsakokeenArviointi,
  isYleisenKielitutkinnonOsakokeenArviointi
} from './YleisenKielitutkinnonOsakokeenArviointi'

/**
 * ArviointiPäivämäärällä
 *
 * @see `fi.oph.koski.schema.ArviointiPäivämäärällä`
 */
export type ArviointiPäivämäärällä =
  | AmmatillinenArviointi
  | IBKurssinArviointi
  | KorkeakoulunKoodistostaLöytyväArviointi
  | KorkeakoulunPaikallinenArviointi
  | LukiodiplominSuoritusJaArviointi
  | LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi
  | LukutaitokoulutuksenArviointi
  | MuunAmmatillisenKoulutuksenArviointi
  | NumeerinenLukionArviointi
  | NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
  | NäytönArviointi
  | OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi
  | PuhviKoe2019
  | SanallinenLukionArviointi
  | SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
  | SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi
  | SuullisenKielitaidonKoe2019
  | TaiteenPerusopetuksenArviointi
  | TelmaJaValmaArviointi
  | VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022
  | ValtionhallinnonKielitutkinnonArviointi
  | VapaanSivistystyöJotpaKoulutuksenArviointi
  | VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi
  | VapaanSivistystyönOsaamismerkinArviointi
  | YleisenKielitutkinnonOsakokeenArviointi

export const isArviointiPäivämäärällä = (a: any): a is ArviointiPäivämäärällä =>
  isAmmatillinenArviointi(a) ||
  isIBKurssinArviointi(a) ||
  isKorkeakoulunKoodistostaLöytyväArviointi(a) ||
  isKorkeakoulunPaikallinenArviointi(a) ||
  isLukiodiplominSuoritusJaArviointi(a) ||
  isLukionOmanÄidinkielenOpinnonOsasuorituksenArviointi(a) ||
  isLukutaitokoulutuksenArviointi(a) ||
  isMuunAmmatillisenKoulutuksenArviointi(a) ||
  isNumeerinenLukionArviointi(a) ||
  isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a) ||
  isNäytönArviointi(a) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
    a
  ) ||
  isPuhviKoe2019(a) ||
  isSanallinenLukionArviointi(a) ||
  isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a) ||
  isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
    a
  ) ||
  isSuullisenKielitaidonKoe2019(a) ||
  isTaiteenPerusopetuksenArviointi(a) ||
  isTelmaJaValmaArviointi(a) ||
  isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022(a) ||
  isValtionhallinnonKielitutkinnonArviointi(a) ||
  isVapaanSivistystyöJotpaKoulutuksenArviointi(a) ||
  isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(a) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
    a
  ) ||
  isVapaanSivistystyönOsaamismerkinArviointi(a) ||
  isYleisenKielitutkinnonOsakokeenArviointi(a)
