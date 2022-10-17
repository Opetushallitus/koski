import React from 'baret'
import Http from '../util/http'
import { tiedonsiirrotContentP, ReloadButton } from './Tiedonsiirrot'
import Link from '../components/Link'
import SortingTableHeader from '../components/SortingTableHeader'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { ISO2FinnishDateTime } from '../date/date'
import { onlyIfHasReadAccess } from '../virkailija/accessCheck'

const yhteenvetoP = (queryString) =>
  Http.cachedGet('/koski/api/tiedonsiirrot/yhteenveto' + queryString, {
    willHandleErrors: true
  })

export const tiedonsiirtojenYhteenvetoContentP = (queryString) =>
  onlyIfHasReadAccess(
    tiedonsiirrotContentP(
      '/koski/tiedonsiirrot/yhteenveto',
      yhteenvetoP(queryString).map((rivit) => ({
        content: (
          <div className="tiedonsiirto-yhteenveto">
            <Text name="Yhteenveto siirretyistä oppilaitoksittain" />
            <ReloadButton />
            <table>
              <thead>
                <tr>
                  <SortingTableHeader
                    field="oppilaitos"
                    titleKey="Oppilaitos"
                    default="asc"
                  />
                  <SortingTableHeader
                    field="aika"
                    titleKey="Viimeisin siirto"
                  />
                  <SortingTableHeader
                    field="siirretyt"
                    titleKey="Siirrettyjen lukumäärä"
                  />
                  <SortingTableHeader
                    field="virheelliset"
                    titleKey="Virheellisten lukumäärä"
                  />
                  <SortingTableHeader
                    field="onnistuneet"
                    titleKey="Onnistuneiden lukumäärä"
                  />
                  <th className="lähdejärjestelmä">
                    <Text name="Lähdejärjestelmä" />
                  </th>
                  <th className="lähdejärjestelmä">
                    <Text name="Käyttäjä" />
                  </th>
                </tr>
              </thead>
              <tbody>
                {rivit.map((rivi, i) => {
                  return (
                    <tr key={i}>
                      <td className="oppilaitos">
                        <Link
                          href={
                            '/koski/tiedonsiirrot?oppilaitos=' +
                            rivi.oppilaitos.oid
                          }
                        >
                          {t(rivi.oppilaitos.nimi)}
                        </Link>
                      </td>
                      <td className="aika">
                        {ISO2FinnishDateTime(rivi.viimeisin)}
                      </td>
                      <td className="siirretyt">{rivi.siirretyt}</td>
                      <td className="virheelliset">
                        {rivi.virheelliset ? (
                          <Link
                            href={
                              '/koski/tiedonsiirrot/virheet?oppilaitos=' +
                              rivi.oppilaitos.oid
                            }
                          >
                            {rivi.virheelliset}
                          </Link>
                        ) : (
                          '0'
                        )}
                      </td>
                      <td className="opiskeluoikeudet">{rivi.onnistuneet}</td>
                      <td className="lähdejärjestelmä">
                        {rivi.lähdejärjestelmä
                          ? t(rivi.lähdejärjestelmä.nimi)
                          : ''}
                      </td>
                      <td className="käyttäjä">
                        {rivi.käyttäjä.käyttäjätunnus || rivi.käyttäjä.oid}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ),
        title: 'Tiedonsiirrot'
      }))
    )
  )
