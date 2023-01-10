import React from 'react'
import Bacon from 'baconjs'
import Pager from '../util/Pager'
import { currentLocation, navigateWithQueryParams } from '../util/location'
import { OppijaHaku } from './OppijaHaku'
import PaginationLink from '../components/PaginationLink'
import * as R from 'ramda'
import * as L from 'partial.lenses'
import DatePicker from '../date/DateRangeSelection'
import OrganisaatioPicker from './OrganisaatioPicker'
import { formatISODate, ISO2FinnishDate } from '../date/date'
import Dropdown from '../components/Dropdown'
import Http from '../util/http'
import SortingTableHeader from '../components/SortingTableHeader'
import delays from '../util/delays'
import Highlight from 'react-highlighter'
import Link from '../components/Link'
import { lang, t } from '../i18n/i18n'
import Text from '../i18n/Text'
import { EditLocalizationsLink } from '../i18n/EditLocalizationsLink'
import { userP } from '../util/user'

export const listviewPath = () => {
  return sessionStorage.previousListViewPath || '/koski/'
}

export class Oppijataulukko extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      opiskeluoikeudenTyypit: [],
      koulutus: [],
      opiskeluoikeudenTila: []
    }
  }

  render() {
    const { total, rivit, edellisetRivit, pager, params } = this.props
    const { opiskeluoikeudenTyypit, koulutus, opiskeluoikeudenTila } =
      this.state
    const näytettävätRivit = rivit || edellisetRivit
    const nullSelection = { value: t('Ei valintaa') }
    sessionStorage.previousListViewPath = currentLocation().toString()

    return (
      <div>
        <h2 className="oppijataulukko-header">
          <Text name="Opiskelijat" />
          <div className="opiskeluoikeudet-total">
            <span className="title label">
              <Text name="Opiskeluoikeuksia" />
            </span>
            <span className="value">{': ' + total}</span>
          </div>
        </h2>
        <div className="oppijataulukko">
          {näytettävätRivit ? (
            <table>
              <thead>
                <tr>
                  <SortingTableHeader
                    field="nimi"
                    titleKey="Nimi"
                    defaultSort="asc"
                  >
                    <input
                      placeholder={t('hae')}
                      type="text"
                      defaultValue={params.nimihaku}
                      onChange={(e) => {
                        if (
                          e.target.value.length >= 3 ||
                          e.target.value.length === 0
                        )
                          this.textFilterBus.push({ nimihaku: e.target.value })
                      }}
                    />
                  </SortingTableHeader>
                  <th className="tyyppi">
                    <span className="title">
                      <Text name="Opiskeluoikeuden tyyppi" />
                    </span>
                    <Dropdown
                      id="tyyppi-valinta"
                      options={[nullSelection].concat(opiskeluoikeudenTyypit)}
                      onSelectionChanged={(option) =>
                        this.filterBus.push({
                          opiskeluoikeudenTyyppi: option.key
                        })
                      }
                      selected={opiskeluoikeudenTyypit.find(
                        (o) => o.key === params.opiskeluoikeudenTyyppi
                      )}
                    />
                  </th>
                  <th className="koulutus">
                    <span className="title">
                      <Text name="Koulutus" />
                    </span>
                    <Dropdown
                      id="koulutus-valinta"
                      options={[nullSelection].concat(koulutus)}
                      onSelectionChanged={(option) =>
                        this.filterBus.push({ suorituksenTyyppi: option.key })
                      }
                      selected={koulutus.find(
                        (o) => o.key === params.suorituksenTyyppi
                      )}
                    />
                  </th>
                  <th className="tutkinto">
                    <span className="title">
                      <Text name="Tutkinto / osaamisala / nimike" />
                    </span>
                    <input
                      placeholder={t('hae')}
                      type="text"
                      defaultValue={params.tutkintohaku}
                      onChange={(e) => {
                        if (
                          e.target.value.length >= 3 ||
                          e.target.value.length === 0
                        )
                          this.textFilterBus.push({
                            tutkintohaku: e.target.value
                          })
                      }}
                    />
                  </th>
                  <th className="tila">
                    <span className="title">
                      <Text name="Tila" />
                    </span>
                    <Dropdown
                      id="tila-valinta"
                      options={[nullSelection].concat(opiskeluoikeudenTila)}
                      onSelectionChanged={(option) =>
                        this.filterBus.push({
                          opiskeluoikeudenTila: option.key
                        })
                      }
                      selected={opiskeluoikeudenTila.find(
                        (o) => o.key === params.opiskeluoikeudenTila
                      )}
                    />
                  </th>
                  <th className="oppilaitos">
                    <span className="title">
                      <Text name="Oppilaitos / toimipiste" />
                    </span>
                    <OrganisaatioPicker
                      selectedOrg={{
                        oid: params.toimipiste,
                        nimi: params.toimipisteNimi
                      }}
                      onSelectionChanged={(org) => {
                        this.filterBus.push(
                          org
                            ? {
                                toimipiste: org.oid,
                                toimipisteNimi: t(org.nimi)
                              }
                            : { toimipiste: null, toimipisteNimi: null }
                        )
                      }}
                      noSelectionText={t('kaikki')}
                      shouldShowChildren={(org) =>
                        org.oid !== 'OSTOPALVELUTAIPALVELUSETELI' &&
                        org.oid !== 'HANKINTAKOULUTUS'
                      }
                    />
                  </th>
                  <SortingTableHeader
                    field="alkamispäivä"
                    titleKey="Aloitus pvm"
                  >
                    <DatePicker
                      selectedStartDay={
                        params.opiskeluoikeusAlkanutAikaisintaan &&
                        ISO2FinnishDate(
                          params.opiskeluoikeusAlkanutAikaisintaan
                        )
                      }
                      selectedEndDay={
                        params['opiskeluoikeusAlkanutViimeistään'] &&
                        ISO2FinnishDate(
                          params['opiskeluoikeusAlkanutViimeistään']
                        )
                      }
                      onSelectionChanged={(range) =>
                        this.filterBus.push({
                          opiskeluoikeusAlkanutAikaisintaan: range.from
                            ? formatISODate(range.from)
                            : undefined,
                          opiskeluoikeusAlkanutViimeistään: range.to
                            ? formatISODate(range.to)
                            : undefined
                        })
                      }
                    />
                  </SortingTableHeader>
                  <SortingTableHeader
                    field="päättymispäivä"
                    titleKey="Päättyminen pvm"
                  >
                    <DatePicker
                      label="Päättymispäivä"
                      selectedStartDay={
                        params['opiskeluoikeusPäättynytAikaisintaan'] &&
                        ISO2FinnishDate(
                          params['opiskeluoikeusPäättynytAikaisintaan']
                        )
                      }
                      selectedEndDay={
                        params['opiskeluoikeusPäättynytViimeistään'] &&
                        ISO2FinnishDate(
                          params['opiskeluoikeusPäättynytViimeistään']
                        )
                      }
                      onSelectionChanged={(range) =>
                        this.filterBus.push({
                          opiskeluoikeusPäättynytAikaisintaan: range.from
                            ? formatISODate(range.from)
                            : undefined,
                          opiskeluoikeusPäättynytViimeistään: range.to
                            ? formatISODate(range.to)
                            : undefined
                        })
                      }
                    />
                  </SortingTableHeader>
                  <SortingTableHeader field="luokka" titleKey="Luokka / ryhmä">
                    <input
                      placeholder={t('hae')}
                      type="text"
                      defaultValue={params.luokkahaku}
                      onChange={(e) =>
                        this.filterBus.push({ luokkahaku: e.target.value })
                      }
                    />
                  </SortingTableHeader>
                </tr>
              </thead>
              <tbody className={rivit ? '' : 'loading'}>
                {näytettävätRivit.map((opiskeluoikeus, i) => {
                  return (
                    <tr className="alternating" key={i}>
                      <td className="nimi">
                        <Link
                          href={`/koski/oppija/${opiskeluoikeus.henkilö.oid}`}
                        >
                          <Highlight
                            ignoreDiacritics={true}
                            diacriticsBlacklist={'åäöÅÄÖ'}
                            search={params.nimihaku || ''}
                          >
                            {opiskeluoikeus.henkilö.sukunimi +
                              ', ' +
                              opiskeluoikeus.henkilö.etunimet}
                          </Highlight>
                        </Link>
                      </td>
                      <td className="tyyppi">
                        {t(opiskeluoikeus.tyyppi.nimi)}
                      </td>
                      <td className="koulutus">
                        <ul className="cell-listing">
                          {opiskeluoikeus.suoritukset.map((suoritus, j) => (
                            <li key={j}>{t(suoritus.tyyppi.nimi)}</li>
                          ))}
                        </ul>
                      </td>
                      <td className="tutkinto">
                        {opiskeluoikeus.suoritukset.map((suoritus, j) => (
                          <ul className="cell-listing" key={j}>
                            {
                              <li className="koulutusmoduuli">
                                <Highlight search={params.tutkintohaku || ''}>
                                  {t(suoritus.koulutusmoduuli.tunniste.nimi)}
                                </Highlight>
                              </li>
                            }
                            {(suoritus.osaamisala || []).map(
                              (osaamisala, k) => (
                                <li className="osaamisala" key={k}>
                                  <Highlight search={params.tutkintohaku || ''}>
                                    {t(osaamisala.nimi)}
                                  </Highlight>
                                </li>
                              )
                            )}
                            {(suoritus.tutkintonimike || []).map(
                              (nimike, k) => (
                                <li className="tutkintonimike" key={k}>
                                  <Highlight search={params.tutkintohaku || ''}>
                                    {t(nimike.nimi)}
                                  </Highlight>
                                </li>
                              )
                            )}
                          </ul>
                        ))}
                      </td>
                      <td className="tila">
                        {t(L.get(['tilat', 0, 'tila', 'nimi'], opiskeluoikeus))}
                      </td>
                      <td className="oppilaitos">
                        <ul className="cell-listing">
                          {opiskeluoikeus.suoritukset.map((suoritus, j) => (
                            <li key={j} className="toimipiste">
                              {t(suoritus.toimipiste.nimi)}
                            </li>
                          ))}
                        </ul>
                      </td>
                      <td className="aloitus pvm">
                        {ISO2FinnishDate(opiskeluoikeus.alkamispäivä)}
                      </td>
                      <td className="päättyminen pvm">
                        {ISO2FinnishDate(opiskeluoikeus.päättymispäivä)}
                      </td>
                      <td className="luokka">
                        <Highlight search={params.luokkahaku || ''}>
                          {opiskeluoikeus.luokka}
                        </Highlight>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          ) : (
            <div className="ajax-indicator-bg">
              <Text name="Ladataan..." />
            </div>
          )}
          <PaginationLink pager={pager} />
        </div>
      </div>
    )
  }

  UNSAFE_componentWillMount() {
    const koodistoMetadata = (k) =>
      k.metadata.find((m) => m.kieli === lang.toUpperCase()) ||
      k.metadata.find((m) => m.kieli === 'FI')
    const koodistoDropdownArvot = (koodit) =>
      koodit
        .filter((k) => k.koodiArvo !== 'mitatoity')
        .map((k) => ({ key: k.koodiArvo, value: koodistoMetadata(k).nimi }))
        .sort((a, b) => a.value.localeCompare(b.value))
    this.filterBus = Bacon.Bus()
    this.textFilterBus = Bacon.Bus()
    const opiskeluoikeudenTyyppiP = this.filterBus
      .filter((x) => 'opiskeluoikeudenTyyppi' in x)
      .map('.opiskeluoikeudenTyyppi')
      .toProperty(this.props.params.opiskeluoikeudenTyyppi)

    const opiskeluoikeudenTyypit = Http.cachedGet(
      '/koski/api/koodisto/opiskeluoikeudentyyppi/latest'
    ).map(koodistoDropdownArvot)
    const koulutus = opiskeluoikeudenTyyppiP
      .flatMap((ot) =>
        Http.cachedGet(
          '/koski/api/koodisto/suoritustyypit' +
            (ot ? '?opiskeluoikeudentyyppi=' + ot : '')
        ).map(koodistoDropdownArvot)
      )
      .toProperty()
    const opiskeluoikeudenTila = Http.cachedGet(
      '/koski/api/koodisto/koskiopiskeluoikeudentila/latest'
    ).map(koodistoDropdownArvot)

    Bacon.combineTemplate({
      opiskeluoikeudenTyypit,
      koulutus,
      opiskeluoikeudenTila
    }).onValue((values) => this.setState(values))

    this.filterBus.plug(
      koulutus
        .filter(
          (suoritusTyypit) =>
            this.props.params.suorituksenTyyppi &&
            !R.includes(
              this.props.params.suorituksenTyyppi,
              R.map((x) => x.key, suoritusTyypit)
            )
        )
        .map(() => R.objOf('suorituksenTyyppi', undefined))
    )
    this.filterBus
      .merge(this.textFilterBus.throttle(delays().delay(500)))
      .onValue(navigateWithQueryParams)
  }
}

let edellisetRivit = null

export const oppijataulukkoContentP = (query, params) => {
  const taulukkoP = userP
    .flatMap((u) => {
      if (u.isViranomainen) {
        return Bacon.once(null)
      } else {
        const pager = Pager(
          '/koski/api/opiskeluoikeus/perustiedot' + query,
          L.prop('tiedot')
        )
        const searchResultsP = pager.rowsP
          .doAction((result) => (edellisetRivit = result.tiedot))
          .startWith(null)
        const totalP = pager.rowsP.map((r) => r.total)

        return Bacon.combineWith(
          searchResultsP,
          totalP,
          (searchResults, total) => (
            <Oppijataulukko
              total={total}
              rivit={searchResults.tiedot}
              edellisetRivit={edellisetRivit}
              pager={pager}
              params={params}
            />
          )
        )
      }
    })
    .toProperty()

  return taulukkoP.map((taulukko) => ({
    content: (
      <div className="content-area">
        <div className="main-content">
          <OppijaHaku />
          <EditLocalizationsLink />
          {taulukko}
        </div>
      </div>
    ),
    title: ''
  }))
}
