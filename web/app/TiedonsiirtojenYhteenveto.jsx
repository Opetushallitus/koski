import React from 'react'
import Http from './http'
import fecha from 'fecha'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'

const yhteenvetoP = () => Http.get('/koski/api/tiedonsiirrot/yhteenveto').toProperty()

export const tiedonsiirtojenYhteenvetoContentP = () => tiedonsiirrotContentP('/koski/tiedonsiirrot/yhteenveto', yhteenvetoP().map((rivit) =>
  (<div>
    Yhteenveto KOSKI-palveluun siirretyistä opiskelijatiedoista oppilaitoksittain.
    <table>
      <thead>
      <tr>
        <th className="oppilaitos">Oppilaitos</th>
        <th className="aika">Viimeisin siirto</th>
        <th className="siirretyt">Siirrettyjen lukumäärä <small>(viimeiset 7pv)</small></th>
        <th className="virheelliset">Virheellisten lukumäärä <small>(viimeiset 7pv)</small></th>
        <th className="opiskeluoikeudet">Opiskelu-oikeuksien lukumäärä</th>
        <th className="lähdejärjestelmä">Lähdejärjestelmä</th>
      </tr>
      </thead>
      <tbody>
     { rivit.map(rivi => <tr>
       <td className="oppilaitos">{rivi.oppilaitos.nimi.fi}</td>
       <td className="aika">{fecha.format(fecha.parse(rivi.viimeisin, 'YYYY-MM-DDThh:mm'), 'D.M.YYYY h:mm')}</td>
       <td className="siirretyt">{rivi.siirretyt}</td>
       <td className="virheelliset">{rivi.virheelliset}</td>
       <td className="opiskeluoikeudet">{rivi.opiskeluoikeudet}</td>
       <td className="lähdejärjestelmä">{rivi.lähdejärjestelmä ? rivi.lähdejärjestelmä.nimi.fi : ''}</td>
      </tr>)
      }
      </tbody>
    </table>
  </div>)
))