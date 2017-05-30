import React from 'react'
import Http from './http'
import fecha from 'fecha'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'
import Link from './Link.jsx'
import SortingTableHeader from './SortingTableHeader.jsx'
import {t} from './i18n'
import Text from './Text.jsx'

const yhteenvetoP = (queryString) => Http.cachedGet('/koski/api/tiedonsiirrot/yhteenveto' + queryString, { willHandleErrors: true})

export const tiedonsiirtojenYhteenvetoContentP = (queryString) => tiedonsiirrotContentP('/koski/tiedonsiirrot/yhteenveto', yhteenvetoP(queryString).map((rivit) =>
  ({
    content: (<div className="tiedonsiirto-yhteenveto">
                <Text name="Yhteenveto siirretyistä oppilaitoksittain"/>
                <table>
                  <thead>
                  <tr>
                    <SortingTableHeader field="oppilaitos" titleKey='Oppilaitos' default="asc"/>
                    <SortingTableHeader field="aika" titleKey='Viimeisin siirto'/>
                    <th className="siirretyt"><Text name="Siirrettyjen lukumäärä"/></th>
                    <th className="virheelliset"><Text name="Virheellisten lukumäärä"/></th>
                    <th className="opiskeluoikeudet"><Text name="Opiskelu-oikeuksien lukumäärä"/></th>
                    <th className="lähdejärjestelmä"><Text name="Lähdejärjestelmä"/></th>
                    <th className="lähdejärjestelmä"><Text name="Käyttäjä"/></th>
                  </tr>
                  </thead>
                  <tbody>
                 { rivit.map((rivi, i) => {
                     return (<tr key={i}>
                       <td className="oppilaitos"><Link href={'/koski/tiedonsiirrot?oppilaitos=' + rivi.oppilaitos.oid}><Text name={rivi.oppilaitos.nimi}/></Link></td>
                       <td className="aika">{fecha.format(fecha.parse(rivi.viimeisin, 'YYYY-MM-DDThh:mm'), 'D.M.YYYY H:mm')}</td>
                       <td className="siirretyt">{rivi.siirretyt}</td>
                       <td className="virheelliset">{ rivi.virheelliset ? <Link href={'/koski/tiedonsiirrot/virheet?oppilaitos=' + rivi.oppilaitos.oid}>{rivi.virheelliset}</Link> : '0'}</td>
                       <td className="opiskeluoikeudet">{rivi.opiskeluoikeudet}</td>
                       <td className="lähdejärjestelmä">{rivi.lähdejärjestelmä ? t(rivi.lähdejärjestelmä.nimi) : ''}</td>
                       <td className="käyttäjä">{rivi.käyttäjä.käyttäjätunnus || rivi.käyttäjä.oid}</td>
                     </tr>)
                    })
                  }
                  </tbody>
                </table>
              </div>),
      title: 'Tiedonsiirrot'
  })
))