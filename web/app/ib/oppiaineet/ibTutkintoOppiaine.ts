import { IBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { IBCASOppiaineenArviointi } from '../../types/fi/oph/koski/schema/IBCASOppiaineenArviointi'
import { IBCASSuoritus } from '../../types/fi/oph/koski/schema/IBCASSuoritus'
import { IBCoreRequirementsArviointi } from '../../types/fi/oph/koski/schema/IBCoreRequirementsArviointi'
import { IBDBCoreSuoritus } from '../../types/fi/oph/koski/schema/IBDBCoreSuoritus'
import { IBDPCoreOppiaine } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaine'
import { IBExtendedEssaySuoritus } from '../../types/fi/oph/koski/schema/IBExtendedEssaySuoritus'
import { IBOppiaineCAS } from '../../types/fi/oph/koski/schema/IBOppiaineCAS'
import { IBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { IBOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBOppiaineExtendedEssay'
import { IBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBOppiaineMuu } from '../../types/fi/oph/koski/schema/IBOppiaineMuu'
import { IBOppiaineTheoryOfKnowledge } from '../../types/fi/oph/koski/schema/IBOppiaineTheoryOfKnowledge'
import { IBTheoryOfKnowledgeSuoritus } from '../../types/fi/oph/koski/schema/IBTheoryOfKnowledgeSuoritus'
import { IBTutkinnonOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusTunneissa } from '../../types/fi/oph/koski/schema/LaajuusTunneissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste
} from './tunnisteet'

export const DPCoreOppiaineet = ['TOK', 'EE', 'CAS']

export type IBOppiaineenSuoritusProps = {
  tunniste?: Koodistokoodiviite<'oppiaineetib'>
  pakollinen?: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  taso?: Koodistokoodiviite<'oppiaineentasoib'>
  extendedEssay?: IBExtendedEssaySuoritusProps
  cas?: IBCASOppiaineenSuoritusProps
}

export type IBCASOppiaineenSuoritusProps = {
  laajuus?: LaajuusTunneissa
}

export const createIBTutkinnonOppiaine = (
  props: IBOppiaineenSuoritusProps
): IBTutkinnonOppiaineenSuoritus | null =>
  createIBDBCoreSuoritus(props) || createIBOppiaineenSuoritus(props)

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

const createIBOppiaineMuu = ({
  tunniste,
  pakollinen,
  ryhmä,
  taso
}: IBOppiaineenSuoritusProps): IBOppiaineMuu | null =>
  isIBOppiaineMuuTunniste(tunniste) && ryhmä && taso
    ? IBOppiaineMuu({ tunniste, ryhmä, pakollinen: !!pakollinen, taso })
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

const createIBCoreRequirementsArviointi = ({
  arvosana
}: IBExtendedEssaySuoritusProps): IBCoreRequirementsArviointi | null =>
  arvosana ? IBCoreRequirementsArviointi({ arvosana }) : null

export type IBCoreRequirementsArviointiProps = {
  pakollinen?: boolean
  arvosana?: Koodistokoodiviite<'arviointiasteikkocorerequirementsib'>
}

export const createIBTheoryOfKnowledgeSuoritus = (
  props: IBCoreRequirementsArviointiProps
): IBTheoryOfKnowledgeSuoritus | null => {
  const koulutusmoduuli = createIBOppiaineTheoryOfKnowledge(props)
  const arviointi = createIBCoreRequirementsArviointi(props)
  return koulutusmoduuli && arviointi
    ? IBTheoryOfKnowledgeSuoritus({ koulutusmoduuli, arviointi: [arviointi] })
    : null
}

const createIBOppiaineTheoryOfKnowledge = ({
  pakollinen
}: IBCoreRequirementsArviointiProps): IBOppiaineTheoryOfKnowledge =>
  IBOppiaineTheoryOfKnowledge({
    pakollinen: !!pakollinen
  })

const createIBOppiaineCAS = ({
  pakollinen,
  cas
}: IBOppiaineenSuoritusProps): IBOppiaineCAS =>
  IBOppiaineCAS({
    pakollinen: !!pakollinen,
    laajuus: cas?.laajuus
  })

const createIBDBCoreSuoritus = (
  props: IBOppiaineenSuoritusProps
): IBDBCoreSuoritus | null => {
  const koulutusmoduuli = createIBDPCoreOppiaine(props)
  return koulutusmoduuli ? IBDBCoreSuoritus({ koulutusmoduuli }) : null
}

const createIBDPCoreOppiaine = (
  props: IBOppiaineenSuoritusProps
): IBDPCoreOppiaine | null => {
  switch (props.tunniste?.koodiarvo) {
    case 'TOK':
      return createIBOppiaineTheoryOfKnowledge(props)
    case 'EE':
      return props.extendedEssay
        ? createIBOppiaineExtendedEssay(props.extendedEssay)
        : null
    case 'CAS':
      return createIBOppiaineCAS(props)
    default:
      return null
  }
}
