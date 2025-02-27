import {
  AikuistenPerusopetuksenPaikallinenOppiaine,
  isAikuistenPerusopetuksenPaikallinenOppiaine
} from './AikuistenPerusopetuksenPaikallinenOppiaine'
import {
  AikuistenPerusopetuksenUskonto,
  isAikuistenPerusopetuksenUskonto
} from './AikuistenPerusopetuksenUskonto'
import {
  AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli,
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
} from './AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
import {
  AikuistenPerusopetuksenÄidinkieliJaKirjallisuus,
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus
} from './AikuistenPerusopetuksenAidinkieliJaKirjallisuus'
import {
  AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli,
  isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
} from './AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'
import {
  AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla,
  isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
} from './AmmatillisenTutkinnonViestintaJaVuorovaikutusKielivalinnalla'
import {
  AmmatillisenTutkinnonÄidinkieli,
  isAmmatillisenTutkinnonÄidinkieli
} from './AmmatillisenTutkinnonAidinkieli'
import { IBKurssi, isIBKurssi } from './IBKurssi'
import { IBOppiaineCAS, isIBOppiaineCAS } from './IBOppiaineCAS'
import {
  IBOppiaineExtendedEssay,
  isIBOppiaineExtendedEssay
} from './IBOppiaineExtendedEssay'
import { IBOppiaineLanguage, isIBOppiaineLanguage } from './IBOppiaineLanguage'
import { IBOppiaineMuu, isIBOppiaineMuu } from './IBOppiaineMuu'
import {
  IBOppiaineTheoryOfKnowledge,
  isIBOppiaineTheoryOfKnowledge
} from './IBOppiaineTheoryOfKnowledge'
import {
  LukionMatematiikka2015,
  isLukionMatematiikka2015
} from './LukionMatematiikka2015'
import {
  LukionMatematiikka2019,
  isLukionMatematiikka2019
} from './LukionMatematiikka2019'
import {
  LukionMuuModuuliMuissaOpinnoissa2019,
  isLukionMuuModuuliMuissaOpinnoissa2019
} from './LukionMuuModuuliMuissaOpinnoissa2019'
import {
  LukionMuuModuuliOppiaineissa2019,
  isLukionMuuModuuliOppiaineissa2019
} from './LukionMuuModuuliOppiaineissa2019'
import {
  LukionMuuValtakunnallinenOppiaine2015,
  isLukionMuuValtakunnallinenOppiaine2015
} from './LukionMuuValtakunnallinenOppiaine2015'
import {
  LukionMuuValtakunnallinenOppiaine2019,
  isLukionMuuValtakunnallinenOppiaine2019
} from './LukionMuuValtakunnallinenOppiaine2019'
import {
  LukionOmanÄidinkielenOpinto,
  isLukionOmanÄidinkielenOpinto
} from './LukionOmanAidinkielenOpinto'
import {
  LukionPaikallinenOpintojakso2019,
  isLukionPaikallinenOpintojakso2019
} from './LukionPaikallinenOpintojakso2019'
import { LukionUskonto2015, isLukionUskonto2015 } from './LukionUskonto2015'
import { LukionUskonto2019, isLukionUskonto2019 } from './LukionUskonto2019'
import {
  LukionVieraanKielenModuuliMuissaOpinnoissa2019,
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019
} from './LukionVieraanKielenModuuliMuissaOpinnoissa2019'
import {
  LukionVieraanKielenModuuliOppiaineissa2019,
  isLukionVieraanKielenModuuliOppiaineissa2019
} from './LukionVieraanKielenModuuliOppiaineissa2019'
import {
  LukionÄidinkielenModuuli2019,
  isLukionÄidinkielenModuuli2019
} from './LukionAidinkielenModuuli2019'
import {
  LukionÄidinkieliJaKirjallisuus2015,
  isLukionÄidinkieliJaKirjallisuus2015
} from './LukionAidinkieliJaKirjallisuus2015'
import {
  LukionÄidinkieliJaKirjallisuus2019,
  isLukionÄidinkieliJaKirjallisuus2019
} from './LukionAidinkieliJaKirjallisuus2019'
import {
  LukioonValmistavaÄidinkieliJaKirjallisuus,
  isLukioonValmistavaÄidinkieliJaKirjallisuus
} from './LukioonValmistavaAidinkieliJaKirjallisuus'
import {
  MuuAikuistenPerusopetuksenOppiaine,
  isMuuAikuistenPerusopetuksenOppiaine
} from './MuuAikuistenPerusopetuksenOppiaine'
import {
  MuuNuortenPerusopetuksenOppiaine,
  isMuuNuortenPerusopetuksenOppiaine
} from './MuuNuortenPerusopetuksenOppiaine'
import {
  MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine,
  isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
} from './MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'
import {
  MuuValtakunnallinenTutkinnonOsa,
  isMuuValtakunnallinenTutkinnonOsa
} from './MuuValtakunnallinenTutkinnonOsa'
import { MuutKielet, isMuutKielet } from './MuutKielet'
import {
  NuortenPerusopetuksenPaikallinenOppiaine,
  isNuortenPerusopetuksenPaikallinenOppiaine
} from './NuortenPerusopetuksenPaikallinenOppiaine'
import {
  NuortenPerusopetuksenUskonto,
  isNuortenPerusopetuksenUskonto
} from './NuortenPerusopetuksenUskonto'
import {
  NuortenPerusopetuksenVierasTaiToinenKotimainenKieli,
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli
} from './NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
import {
  NuortenPerusopetuksenÄidinkieliJaKirjallisuus,
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus
} from './NuortenPerusopetuksenAidinkieliJaKirjallisuus'
import {
  PaikallinenAmmatillisenTutkinnonOsanOsaAlue,
  isPaikallinenAmmatillisenTutkinnonOsanOsaAlue
} from './PaikallinenAmmatillisenTutkinnonOsanOsaAlue'
import {
  PaikallinenLukionOppiaine2015,
  isPaikallinenLukionOppiaine2015
} from './PaikallinenLukionOppiaine2015'
import {
  PaikallinenLukionOppiaine2019,
  isPaikallinenLukionOppiaine2019
} from './PaikallinenLukionOppiaine2019'
import {
  PaikallinenLukioonValmistavanKoulutuksenOppiaine,
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine
} from './PaikallinenLukioonValmistavanKoulutuksenOppiaine'
import {
  PaikallinenTelmaKoulutuksenOsa,
  isPaikallinenTelmaKoulutuksenOsa
} from './PaikallinenTelmaKoulutuksenOsa'
import {
  PaikallinenTutkinnonOsa,
  isPaikallinenTutkinnonOsa
} from './PaikallinenTutkinnonOsa'
import {
  PaikallinenValmaKoulutuksenOsa,
  isPaikallinenValmaKoulutuksenOsa
} from './PaikallinenValmaKoulutuksenOsa'
import {
  ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue,
  isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue
} from './ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'
import {
  VierasTaiToinenKotimainenKieli2015,
  isVierasTaiToinenKotimainenKieli2015
} from './VierasTaiToinenKotimainenKieli2015'
import {
  VierasTaiToinenKotimainenKieli2019,
  isVierasTaiToinenKotimainenKieli2019
} from './VierasTaiToinenKotimainenKieli2019'
import {
  YhteinenTutkinnonOsa,
  isYhteinenTutkinnonOsa
} from './YhteinenTutkinnonOsa'

/**
 * Valinnaisuus
 *
 * @see `fi.oph.koski.schema.Valinnaisuus`
 */
export type Valinnaisuus =
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | AikuistenPerusopetuksenUskonto
  | AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
  | AikuistenPerusopetuksenÄidinkieliJaKirjallisuus
  | AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
  | AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
  | AmmatillisenTutkinnonÄidinkieli
  | IBKurssi
  | IBOppiaineCAS
  | IBOppiaineExtendedEssay
  | IBOppiaineLanguage
  | IBOppiaineMuu
  | IBOppiaineTheoryOfKnowledge
  | LukionMatematiikka2015
  | LukionMatematiikka2019
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionMuuModuuliOppiaineissa2019
  | LukionMuuValtakunnallinenOppiaine2015
  | LukionMuuValtakunnallinenOppiaine2019
  | LukionOmanÄidinkielenOpinto
  | LukionPaikallinenOpintojakso2019
  | LukionUskonto2015
  | LukionUskonto2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliOppiaineissa2019
  | LukionÄidinkielenModuuli2019
  | LukionÄidinkieliJaKirjallisuus2015
  | LukionÄidinkieliJaKirjallisuus2019
  | LukioonValmistavaÄidinkieliJaKirjallisuus
  | MuuAikuistenPerusopetuksenOppiaine
  | MuuNuortenPerusopetuksenOppiaine
  | MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
  | MuuValtakunnallinenTutkinnonOsa
  | MuutKielet
  | NuortenPerusopetuksenPaikallinenOppiaine
  | NuortenPerusopetuksenUskonto
  | NuortenPerusopetuksenVierasTaiToinenKotimainenKieli
  | NuortenPerusopetuksenÄidinkieliJaKirjallisuus
  | PaikallinenAmmatillisenTutkinnonOsanOsaAlue
  | PaikallinenLukionOppiaine2015
  | PaikallinenLukionOppiaine2019
  | PaikallinenLukioonValmistavanKoulutuksenOppiaine
  | PaikallinenTelmaKoulutuksenOsa
  | PaikallinenTutkinnonOsa
  | PaikallinenValmaKoulutuksenOsa
  | ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue
  | VierasTaiToinenKotimainenKieli2015
  | VierasTaiToinenKotimainenKieli2019
  | YhteinenTutkinnonOsa

export const isValinnaisuus = (a: any): a is Valinnaisuus =>
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenUskonto(a) ||
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(a) ||
  isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(a) ||
  isAmmatillisenTutkinnonÄidinkieli(a) ||
  isIBKurssi(a) ||
  isIBOppiaineCAS(a) ||
  isIBOppiaineExtendedEssay(a) ||
  isIBOppiaineLanguage(a) ||
  isIBOppiaineMuu(a) ||
  isIBOppiaineTheoryOfKnowledge(a) ||
  isLukionMatematiikka2015(a) ||
  isLukionMatematiikka2019(a) ||
  isLukionMuuModuuliMuissaOpinnoissa2019(a) ||
  isLukionMuuModuuliOppiaineissa2019(a) ||
  isLukionMuuValtakunnallinenOppiaine2015(a) ||
  isLukionMuuValtakunnallinenOppiaine2019(a) ||
  isLukionOmanÄidinkielenOpinto(a) ||
  isLukionPaikallinenOpintojakso2019(a) ||
  isLukionUskonto2015(a) ||
  isLukionUskonto2019(a) ||
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019(a) ||
  isLukionVieraanKielenModuuliOppiaineissa2019(a) ||
  isLukionÄidinkielenModuuli2019(a) ||
  isLukionÄidinkieliJaKirjallisuus2015(a) ||
  isLukionÄidinkieliJaKirjallisuus2019(a) ||
  isLukioonValmistavaÄidinkieliJaKirjallisuus(a) ||
  isMuuAikuistenPerusopetuksenOppiaine(a) ||
  isMuuNuortenPerusopetuksenOppiaine(a) ||
  isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(a) ||
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isMuutKielet(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isNuortenPerusopetuksenUskonto(a) ||
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isPaikallinenAmmatillisenTutkinnonOsanOsaAlue(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isPaikallinenLukionOppiaine2019(a) ||
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine(a) ||
  isPaikallinenTelmaKoulutuksenOsa(a) ||
  isPaikallinenTutkinnonOsa(a) ||
  isPaikallinenValmaKoulutuksenOsa(a) ||
  isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(a) ||
  isVierasTaiToinenKotimainenKieli2015(a) ||
  isVierasTaiToinenKotimainenKieli2019(a) ||
  isYhteinenTutkinnonOsa(a)
