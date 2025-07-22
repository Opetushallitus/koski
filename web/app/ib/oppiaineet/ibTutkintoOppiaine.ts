import { IBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { IBCASOppiaineenArviointi } from '../../types/fi/oph/koski/schema/IBCASOppiaineenArviointi'
import { IBCASSuoritus } from '../../types/fi/oph/koski/schema/IBCASSuoritus'
import { IBCoreRequirementsArviointi } from '../../types/fi/oph/koski/schema/IBCoreRequirementsArviointi'
import { IBDPCoreSuoritus } from '../../types/fi/oph/koski/schema/IBDPCoreSuoritus'
import { IBDPCoreOppiaine } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaine'
import { IBExtendedEssaySuoritus } from '../../types/fi/oph/koski/schema/IBExtendedEssaySuoritus'
import { IBOppiaineCAS } from '../../types/fi/oph/koski/schema/IBOppiaineCAS'
import { IBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { IBOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBOppiaineExtendedEssay'
import { IBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBOppiaineMuu } from '../../types/fi/oph/koski/schema/IBOppiaineMuu'
import { IBTutkinnonOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusTunneissa } from '../../types/fi/oph/koski/schema/LaajuusTunneissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste
} from './tunnisteet'
import { IBDPCoreOppiaineTheoryOfKnowledge } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineTheoryOfKnowledge'
import { IBDPCoreOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineExtendedEssay'
import { IBDPCoreAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBDPCoreAineRyhmaOppiaine'
import { IBDPCoreOppiaineCAS } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineCAS'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { IBDPCoreOppiaineMuu } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineMuu'
import { IBDPCoreOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineLanguage'

export const DPCoreOppiaineet = ['TOK', 'EE', 'CAS']

export type IBOppiaineenSuoritusProps = {
  tunniste?: Koodistokoodiviite<'oppiaineetib'>
  pakollinen?: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  taso?: Koodistokoodiviite<'oppiaineentasoib'>
  extendedEssay?: IBExtendedEssaySuoritusProps
  cas?: IBCASOppiaineenSuoritusProps
  tok?: IBTOKOppiaineenSuoritusProps
}

export type IBCASOppiaineenSuoritusProps = {
  laajuus?: LaajuusOpintopisteissä
}

export type IBTOKOppiaineenSuoritusProps = {
  laajuus?: LaajuusOpintopisteissä
}

export const createIBTutkinnonOppiaine = (
  props: IBOppiaineenSuoritusProps
): IBTutkinnonOppiaineenSuoritus | null =>
  createIBDPCoreSuoritus(props) || createIBOppiaineenSuoritus(props)

const createIBOppiaineenSuoritus = (
  props: IBOppiaineenSuoritusProps
): IBOppiaineenSuoritus | null => {
  const koulutusmoduuli = createIBAineRyhmäOppiaine(props)
  return (
    koulutusmoduuli &&
    IBOppiaineenSuoritus({
      koulutusmoduuli
    })
  )
}

const createIBAineRyhmäOppiaine = (
  props: IBOppiaineenSuoritusProps
): IBAineRyhmäOppiaine | null =>
  createIBOppiaineLanguage(props) || createIBOppiaineMuu(props)

const createIBDPCoreAineRyhmäOppiaine = (
  props: IBExtendedEssaySuoritusProps
): IBDPCoreAineRyhmäOppiaine | null =>
  createIBOppiaineLanguageForDPCore(props) ||
  createIBOppiaineMuuForDPCore(props)

const createIBOppiaineLanguage = ({
  tunniste,
  pakollinen,
  kieli,
  ryhmä,
  taso
}: IBOppiaineenSuoritusProps): IBOppiaineLanguage | null =>
  isIBOppiaineLanguageTunniste(tunniste) && kieli && ryhmä && taso
    ? IBOppiaineLanguage({
        tunniste,
        kieli,
        ryhmä,
        pakollinen: !!pakollinen,
        taso
      })
    : null

const createIBOppiaineLanguageForDPCore = ({
  tunniste,
  kieli,
  ryhmä,
  taso,
  laajuus
}: IBExtendedEssaySuoritusProps): IBDPCoreOppiaineLanguage | null =>
  isIBOppiaineLanguageTunniste(tunniste) && kieli && ryhmä && taso
    ? IBDPCoreOppiaineLanguage({
        tunniste,
        kieli,
        ryhmä,
        taso,
        laajuus
      })
    : null

const createIBOppiaineMuu = ({
  tunniste,
  pakollinen,
  ryhmä,
  taso
}: IBOppiaineenSuoritusProps): IBOppiaineMuu | null =>
  isIBOppiaineMuuTunniste(tunniste) && ryhmä && taso
    ? IBOppiaineMuu({ tunniste, ryhmä, pakollinen: !!pakollinen, taso })
    : null

const createIBOppiaineMuuForDPCore = ({
  tunniste,
  ryhmä,
  taso,
  laajuus
}: IBExtendedEssaySuoritusProps): IBDPCoreOppiaineMuu | null =>
  isIBOppiaineMuuTunniste(tunniste) && ryhmä && taso
    ? IBDPCoreOppiaineMuu({ tunniste, ryhmä, taso, laajuus })
    : null

export const createIBCASSuoritus = (
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', 'S'>,
  laajuus?: LaajuusTunneissa,
  pakollinen?: boolean
): IBCASSuoritus =>
  IBCASSuoritus({
    koulutusmoduuli: IBOppiaineCAS({
      pakollinen: !!pakollinen,
      laajuus
    }),
    arviointi: [IBCASOppiaineenArviointi({ arvosana })]
  })

export type IBExtendedEssaySuoritusProps = {
  tunniste?: Koodistokoodiviite<'oppiaineetib'>
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  taso?: Koodistokoodiviite<'oppiaineentasoib'>
  aihe?: LocalizedString
  laajuus?: LaajuusOpintopisteissä
  pakollinen?: boolean
  arvosana?: Koodistokoodiviite<'arviointiasteikkocorerequirementsib'>
}

export const createIBExtendedEssaySuoritus = (
  props: IBExtendedEssaySuoritusProps
): IBExtendedEssaySuoritus | null => {
  const koulutusmoduuli = createIBOppiaineExtendedEssay(props)
  const arviointi = createIBCoreRequirementsArviointi(props)
  return (
    koulutusmoduuli &&
    arviointi &&
    IBExtendedEssaySuoritus({ koulutusmoduuli, arviointi: [arviointi] })
  )
}

const createIBOppiaineExtendedEssay = (
  props: IBExtendedEssaySuoritusProps
): IBOppiaineExtendedEssay | null => {
  const aine = createIBAineRyhmäOppiaine(props)
  return aine && props.aihe
    ? IBOppiaineExtendedEssay({
        aine,
        aihe: props.aihe,
        pakollinen: !!props.pakollinen
      })
    : null
}

const createIBDPCoreOppiaineExtendedEssay = (
  props: IBExtendedEssaySuoritusProps,
  pakollinen: boolean | undefined
): IBDPCoreOppiaineExtendedEssay | null => {
  const aine = createIBDPCoreAineRyhmäOppiaine(props)
  return aine && props.aihe
    ? IBDPCoreOppiaineExtendedEssay({
        aine,
        aihe: props.aihe,
        pakollinen: !!pakollinen
      })
    : null
}

const createIBCoreRequirementsArviointi = ({
  arvosana
}: IBExtendedEssaySuoritusProps): IBCoreRequirementsArviointi | null =>
  arvosana ? IBCoreRequirementsArviointi({ arvosana }) : null

const createIBDPCoreOppiaineTheoryOfKnowledge = ({
  pakollinen,
  tok
}: IBOppiaineenSuoritusProps): IBDPCoreOppiaineTheoryOfKnowledge =>
  IBDPCoreOppiaineTheoryOfKnowledge({
    pakollinen: !!pakollinen,
    laajuus: tok?.laajuus
  })

const createIBDPCoreOppiaineCAS = ({
  pakollinen,
  cas
}: IBOppiaineenSuoritusProps): IBDPCoreOppiaineCAS =>
  IBDPCoreOppiaineCAS({
    pakollinen: !!pakollinen,
    laajuus: cas?.laajuus
  })

const createIBDPCoreSuoritus = (
  props: IBOppiaineenSuoritusProps
): IBDPCoreSuoritus | null => {
  const koulutusmoduuli = createIBDPCoreOppiaine(props)
  return koulutusmoduuli ? IBDPCoreSuoritus({ koulutusmoduuli }) : null
}

const createIBDPCoreOppiaine = (
  props: IBOppiaineenSuoritusProps
): IBDPCoreOppiaine | null => {
  switch (props.tunniste?.koodiarvo) {
    case 'TOK':
      return createIBDPCoreOppiaineTheoryOfKnowledge(props)
    case 'EE':
      return props.extendedEssay
        ? createIBDPCoreOppiaineExtendedEssay(
            props.extendedEssay,
            props.pakollinen
          )
        : null
    case 'CAS':
      return createIBDPCoreOppiaineCAS(props)
    default:
      return null
  }
}
