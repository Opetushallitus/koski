import React, { useCallback, useEffect, useMemo } from 'react'
import {
  useApiMethod,
  useApiOnce,
  useLocalDataCopy,
  useOnApiSuccess
} from '../../api-fetch'
import { ISO2FinnishDate, formatFinnishDateTime } from '../../date/date'
import { t } from '../../i18n/i18n'
import { isCompleteQueryResponse } from '../../types/fi/oph/koski/massaluovutus/CompleteQueryResponse'
import { isFailedQueryResponse } from '../../types/fi/oph/koski/massaluovutus/FailedQueryResponse'
import { isPendingQueryResponse } from '../../types/fi/oph/koski/massaluovutus/PendingQueryResponse'
import { QueryResponse } from '../../types/fi/oph/koski/massaluovutus/QueryResponse'
import { isRunningQueryResponse } from '../../types/fi/oph/koski/massaluovutus/RunningQueryResponse'
import {
  createMassaluovutusKysely,
  fetchOmatMassaluovutusKyselyt
} from '../../util/koskiApi'
import { useInterval } from '../../util/useInterval'
import {
  MassaluovutusKyselymalli,
  RaporttiKyselynParametrit,
  RaporttiKyselynTiedot
} from './massaluovutusRaporttiKyselyt'

const pollausväliMs = 3000

export type MassaluovutusRaportinLatausProps = {
  kyselymalli: MassaluovutusKyselymalli
  parametrit: RaporttiKyselynParametrit | null
  dbUpdated?: string
  oppilaitosNimet: Record<string, string>
}

type OmaRaportti = {
  kysely: QueryResponse
  tiedot: RaporttiKyselynTiedot
}

export const MassaluovutusRaportinLataus: React.FC<
  MassaluovutusRaportinLatausProps
> = ({ kyselymalli, parametrit, dbUpdated, oppilaitosNimet }) => {
  const kyselyt = useApiOnce(fetchOmatMassaluovutusKyselyt)
  const [kyselytData] = useLocalDataCopy(kyselyt)
  const luoKysely = useApiMethod(createMassaluovutusKysely)

  const omatRaportit = useMemo(
    () =>
      (kyselytData || [])
        .map((kysely) => ({ kysely, tiedot: kyselymalli.lue(kysely.query) }))
        .filter((r): r is OmaRaportti => r.tiedot !== null),
    [kyselytData, kyselymalli]
  )

  const päivitäLista = kyselyt.call
  const pollaaja = useInterval(päivitäLista, pollausväliMs)

  const keskeneräisiä = omatRaportit.some((r) => onKesken(r.kysely))
  const muodostetaan = keskeneräisiä || luoKysely.state === 'loading'

  useEffect(() => {
    if (keskeneräisiä) {
      pollaaja.start()
    } else {
      pollaaja.stop()
    }
  }, [keskeneräisiä, pollaaja])

  useOnApiSuccess(luoKysely, () => {
    päivitäLista()
  })

  const aloita = useCallback(() => {
    if (parametrit) {
      luoKysely.call(kyselymalli.luo(parametrit))
    }
  }, [luoKysely, kyselymalli, parametrit])

  return (
    <>
      <div className="raportin-lataus">
        {muodostetaan && (
          <div className="ohje" data-testid="massaluovutus.taustalla">
            {t(
              'Raportin muodostaminen jatkuu taustalla. Voit odottaa tai sulkea sivun ja palata lataamaan raportin myöhemmin.'
            )}
          </div>
        )}
        {dbUpdated && <Päivitysaika aika={dbUpdated} />}
        <div className="raportti-download-button">
          <button
            className="koski-button"
            disabled={parametrit === null || luoKysely.state === 'loading'}
            onClick={aloita}
            data-testid="massaluovutus.start"
          >
            {t('Muodosta Excel-tiedosto')}
          </button>
        </div>
        {luoKysely.state === 'error' && (
          <div className="error-text" data-testid="massaluovutus.error">
            {t('Raportin muodostaminen epäonnistui')}
          </div>
        )}
      </div>
      {omatRaportit.length > 0 && (
        <div
          className="massaluovutus-raportit"
          data-testid="massaluovutus.raportit"
        >
          <h3>{t('Omat raportit')}</h3>
          <table>
            <thead>
              <tr>
                <th>{t('Muodostettu')}</th>
                <th>{t('Oppilaitos')}</th>
                <th>{t('Aikajakso')}</th>
                {kyselymalli.näytäOsasuoritustenRajaus && (
                  <th>{t('Tutkinnon osat')}</th>
                )}
                <th>{t('Tiedosto')}</th>
                <th>{t('Salasana')}</th>
              </tr>
            </thead>
            <tbody>
              {omatRaportit.map(({ kysely, tiedot }) => (
                <RaporttiRivi
                  key={kysely.queryId}
                  kysely={kysely}
                  tiedot={tiedot}
                  näytäOsasuoritustenRajaus={
                    kyselymalli.näytäOsasuoritustenRajaus
                  }
                  oppilaitosNimet={oppilaitosNimet}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}

const RaporttiRivi: React.FC<{
  kysely: QueryResponse
  tiedot: RaporttiKyselynTiedot
  näytäOsasuoritustenRajaus: boolean
  oppilaitosNimet: Record<string, string>
}> = ({ kysely, tiedot, näytäOsasuoritustenRajaus, oppilaitosNimet }) => (
  <tr>
    <td className="luontiaika">
      {formatFinnishDateTime(new Date(kysely.createdAt))}
    </td>
    <td className="oppilaitos">{oppilaitos(tiedot, oppilaitosNimet)}</td>
    <td className="aikavali">{aikaväli(tiedot)}</td>
    {näytäOsasuoritustenRajaus && (
      <td className="osasuoritukset">{osasuoritustenRajaus(tiedot)}</td>
    )}
    <td className="tiedosto">
      {isCompleteQueryResponse(kysely) && (
        <a href={kysely.files[0]}>{t('Lataa Excel-tiedosto')}</a>
      )}
      {onKesken(kysely) && (
        <span className="tila">
          {isPendingQueryResponse(kysely)
            ? t('Jonossa')
            : t('Muodostetaan raporttia')}
        </span>
      )}
      {isFailedQueryResponse(kysely) && (
        <span className="error-text">
          {kysely.hint || t('Raportin muodostaminen epäonnistui')}
        </span>
      )}
    </td>
    <td className="password">
      {isCompleteQueryResponse(kysely) ? kysely.password : ''}
    </td>
  </tr>
)

const Päivitysaika: React.FC<{ aika: string }> = ({ aika }) => {
  const [head, foot] = t(
    'Raportti pohjautuu KOSKI-tietovarannossa hetkellä $DATETIME olleille tiedoille.'
  ).split('$DATETIME')
  return (
    <div className="update-time">
      {head}
      <span className="datetime">{formatFinnishDateTime(new Date(aika))}</span>
      {foot}
    </div>
  )
}

const onKesken = (q: QueryResponse): boolean =>
  isPendingQueryResponse(q) || isRunningQueryResponse(q)

const aikaväli = (tiedot: RaporttiKyselynTiedot): string =>
  `${ISO2FinnishDate(tiedot.alku)} – ${ISO2FinnishDate(tiedot.loppu)}`

const oppilaitos = (
  tiedot: RaporttiKyselynTiedot,
  oppilaitosNimet: Record<string, string>
): string =>
  tiedot.organisaatioOid
    ? oppilaitosNimet[tiedot.organisaatioOid] || tiedot.organisaatioOid
    : ''

const osasuoritustenRajaus = (tiedot: RaporttiKyselynTiedot): string =>
  tiedot.osasuoritustenAikarajaus ? t('Aikajaksolta') : t('Kaikki')
