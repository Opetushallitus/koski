import { IBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { IBCASOppiaineenArviointi } from '../../types/fi/oph/koski/schema/IBCASOppiaineenArviointi'
import { IBCASSuoritus } from '../../types/fi/oph/koski/schema/IBCASSuoritus'
import { IBCoreRequirementsArviointi } from '../../types/fi/oph/koski/schema/IBCoreRequirementsArviointi'
import { IBExtendedEssaySuoritus } from '../../types/fi/oph/koski/schema/IBExtendedEssaySuoritus'
import { IBOppiaineCAS } from '../../types/fi/oph/koski/schema/IBOppiaineCAS'
import { IBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { IBOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBOppiaineExtendedEssay'
import { IBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { IBOppiaineMuu } from '../../types/fi/oph/koski/schema/IBOppiaineMuu'
import { IBOppiaineTheoryOfKnowledge } from '../../types/fi/oph/koski/schema/IBOppiaineTheoryOfKnowledge'
import { IBTheoryOfKnowledgeSuoritus } from '../../types/fi/oph/koski/schema/IBTheoryOfKnowledgeSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste
} from './tunnisteet'

export type IBOppiaineenSuoritusProps = {
  tunniste?: Koodistokoodiviite<'oppiaineetib'>
  pakollinen?: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  taso?: Koodistokoodiviite<'oppiaineentasoib'>
}

export const createIBOppiaineenSuoritus = (
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
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', 'S'>
): IBCASSuoritus =>
  IBCASSuoritus({
    koulutusmoduuli: IBOppiaineCAS({
      pakollinen: true
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
}: IBCoreRequirementsArviointiProps): IBOppiaineTheoryOfKnowledge => {
  return IBOppiaineTheoryOfKnowledge({ pakollinen: !!pakollinen })
}
