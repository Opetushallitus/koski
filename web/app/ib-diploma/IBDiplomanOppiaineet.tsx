import React from 'react'
import { TestIdLayer, TestIdText } from '../appstate/useTestId'
import { SuorituksenTilaIcon } from '../components-v2/opiskeluoikeus/OppiaineTable'
import { t } from '../i18n/i18n'
import { isIBDiplomanOppiaineenSuoritus } from '../types/fi/oph/koski/schema/IBDiplomanOppiaineenSuoritus'
import { IBDiplomanSuoritus } from '../types/fi/oph/koski/schema/IBDiplomanSuoritus'
import { parasArviointi } from '../util/arvioinnit'
import { formatExaminationSession, oppiaineenNimi } from './ibDiploma'

export type IBDiplomanOppiaineetProps = {
  suoritus: IBDiplomanSuoritus
}

export const IBDiplomanOppiaineet: React.FC<IBDiplomanOppiaineetProps> = ({
  suoritus
}) => {
  const oppiaineet = (suoritus.osasuoritukset || []).filter(
    isIBDiplomanOppiaineenSuoritus
  )
  if (oppiaineet.length === 0) {
    return null
  }
  return (
    <>
      <table className="IBDiplomanOppiaineet">
        <thead>
          <tr>
            <th>{t('Examination session')}</th>
            <th>{t('Oppiaine')}</th>
            <th className="IBDiplomanOppiaineet__taso">{t('Taso')}</th>
            <th className="IBDiplomanOppiaineet__arvosana">{t('Arvosana')}</th>
          </tr>
        </thead>
        <TestIdLayer id="oppiaineet">
          <tbody>
            {oppiaineet.map((oppiaine, index) => (
              <TestIdLayer key={index} id={index}>
                <tr>
                  <td>
                    <TestIdText id="examinationSession">
                      {formatExaminationSession(oppiaine.examinationSession)}
                    </TestIdText>
                  </td>
                  <td>
                    <div className="IBDiplomanOppiaineet__oppiaine">
                      <div className="IBDiplomanOppiaineet__icon">
                        <SuorituksenTilaIcon suoritus={oppiaine} />
                      </div>
                      <TestIdText id="nimi">
                        {oppiaineenNimi(oppiaine.koulutusmoduuli)}
                      </TestIdText>
                      {oppiaine.additionalSubject && (
                        <TestIdText id="additionalSubject">{'*'}</TestIdText>
                      )}
                    </div>
                  </td>
                  <td className="IBDiplomanOppiaineet__taso">
                    <TestIdText id="taso">
                      {oppiaine.koulutusmoduuli.taso?.koodiarvo}
                    </TestIdText>
                  </td>
                  <td className="IBDiplomanOppiaineet__arvosana">
                    <TestIdText id="arvosana">
                      {parasArviointi(oppiaine.arviointi)?.arvosana.koodiarvo}
                    </TestIdText>
                  </td>
                </tr>
              </TestIdLayer>
            ))}
          </tbody>
        </TestIdLayer>
      </table>
      {oppiaineet.some((oppiaine) => oppiaine.additionalSubject) && (
        <p className="IBDiplomanOppiaineet__selite">
          {`* = ${t('Additional subject')}`}
        </p>
      )}
    </>
  )
}
