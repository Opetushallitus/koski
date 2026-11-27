import React from 'react'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { KoulutuksenKoodiarvo } from '../components-v2/opiskeluoikeus/KoulutuksenKoodiarvo'
import { t } from '../i18n/i18n'
import {
  IBDiplomanCoreSuoritus,
  isIBDiplomanCoreSuoritus
} from '../types/fi/oph/koski/schema/IBDiplomanCoreSuoritus'
import { isIBDiplomanExtendedEssay } from '../types/fi/oph/koski/schema/IBDiplomanExtendedEssay'
import { IBDiplomanSuoritus } from '../types/fi/oph/koski/schema/IBDiplomanSuoritus'
import { isIBDiplomanTheoryOfKnowledge } from '../types/fi/oph/koski/schema/IBDiplomanTheoryOfKnowledge'
import { parasArviointi } from '../util/arvioinnit'
import {
  arvosanaJaNimi,
  formatExaminationSession,
  oppiaineenNimi
} from './ibDiploma'

export type IBDiplomanSuoritusTiedotProps = {
  suoritus: IBDiplomanSuoritus
}

export const IBDiplomanSuoritusTiedot: React.FC<
  IBDiplomanSuoritusTiedotProps
> = ({ suoritus }) => {
  const coreSuoritukset = (suoritus.osasuoritukset || []).filter(
    isIBDiplomanCoreSuoritus
  )
  const extendedEssay = coreSuoritukset.find((s) =>
    isIBDiplomanExtendedEssay(s.koulutusmoduuli)
  )
  const theoryOfKnowledge = coreSuoritukset.find((s) =>
    isIBDiplomanTheoryOfKnowledge(s.koulutusmoduuli)
  )

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Koulutus">
        <TestIdText id="koulutus">
          {t(suoritus.koulutusmoduuli.tunniste.nimi)}
        </TestIdText>{' '}
        <KoulutuksenKoodiarvo
          koodiarvo={suoritus.koulutusmoduuli.tunniste.koodiarvo}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Oppilaitos / toimipiste">
        <TestIdText id="toimipiste">{t(suoritus.toimipiste.nimi)}</TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Examination session">
        <TestIdText id="examinationSession">
          {formatExaminationSession(suoritus.examinationSession)}
        </TestIdText>
      </KeyValueRow>
      {extendedEssay && <ExtendedEssayRows suoritus={extendedEssay} />}
      {theoryOfKnowledge && (
        <TestIdLayer id="theoryOfKnowledge">
          <KeyValueRow localizableLabel="Theory of knowledge">
            <KeyValueTable>
              <CoreArvosanaRow suoritus={theoryOfKnowledge} />
            </KeyValueTable>
          </KeyValueRow>
        </TestIdLayer>
      )}
      <KeyValueRow
        localizableLabel="Combined grade"
        hideIfEmpty={suoritus.combinedGrade !== undefined}
      >
        <TestIdText id="combinedGrade">{suoritus.combinedGrade}</TestIdText>
      </KeyValueRow>
      <KeyValueRow
        localizableLabel="Total points"
        hideIfEmpty={suoritus.totalPoints !== undefined}
      >
        <TestIdText id="totalPoints">{suoritus.totalPoints}</TestIdText>
      </KeyValueRow>
      <KeyValueRow localizableLabel="Result">
        <TestIdText id="result">
          {t(suoritus.result.nimi) || suoritus.result.koodiarvo}
        </TestIdText>
      </KeyValueRow>
    </KeyValueTable>
  )
}

type CoreSuoritusProps = {
  suoritus: IBDiplomanCoreSuoritus
}

const ExtendedEssayRows: React.FC<CoreSuoritusProps> = ({ suoritus }) => {
  const aine = isIBDiplomanExtendedEssay(suoritus.koulutusmoduuli)
    ? suoritus.koulutusmoduuli.aine
    : undefined
  return (
    <TestIdLayer id="extendedEssay">
      <KeyValueRow localizableLabel="Extended essay">
        <KeyValueTable>
          {aine && (
            <KeyValueRow localizableLabel="Oppiaine" innerKeyValueTable>
              <TestIdText id="oppiaine">{oppiaineenNimi(aine)}</TestIdText>
            </KeyValueRow>
          )}
          {aine?.taso && (
            <KeyValueRow localizableLabel="Taso" innerKeyValueTable>
              <TestIdText id="taso">{t(aine.taso.nimi)}</TestIdText>
            </KeyValueRow>
          )}
          <CoreArvosanaRow suoritus={suoritus} />
        </KeyValueTable>
      </KeyValueRow>
    </TestIdLayer>
  )
}

const CoreArvosanaRow: React.FC<CoreSuoritusProps> = ({ suoritus }) => {
  const arviointi = parasArviointi(suoritus.arviointi)
  return arviointi ? (
    <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
      <TestIdText id="arvosana">
        {arvosanaJaNimi(arviointi.arvosana)}
      </TestIdText>
    </KeyValueRow>
  ) : null
}
