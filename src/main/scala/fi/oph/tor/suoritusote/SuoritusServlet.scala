package fi.oph.tor.suoritusote

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.schema.{Opiskeluoikeus, Suoritus, TaydellisetHenkilötiedot}
import fi.oph.tor.servlet.ErrorHandlingServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

import scala.xml.Node

class SuoritusServlet(
  val userRepository: UserOrganisationsRepository,
  val directoryClient: DirectoryClient,
  val rekisteri: TodennetunOsaamisenRekisteri,
  val oppijaRepository: OppijaRepository,
  val opiskeluOikeusRepository: OpiskeluOikeusRepository) extends ErrorHandlingServlet with RequiresAuthentication {



  get("/:oppijaOid/:opiskeluoikeusId") {
    val oid = params("oppijaOid")
    val opiskeluoikeusId = params("opiskeluoikeusId")

    opiskeluOikeusRepository.findByOppijaOid(oid)(torUser).find(oo => oo.tyyppi.koodiarvo == "korkeakoulutus" && oo.lähdejärjestelmänId.exists(_.id == opiskeluoikeusId)).flatMap(oo => oppijaRepository.findByOid(oid).map((_, oo))) match {
      case Some((ht, oo)) => renderSuoritusote(ht, oo)
      case _ => TorErrorCategory.notFound
    }
  }

  def renderSuoritusote(ht: TaydellisetHenkilötiedot, oo: Opiskeluoikeus) = {

    def suoritusWithDepth(t: (Int, Suoritus)) : List[(Int, Suoritus)] = {
      t :: t._2.osasuoritusLista.flatMap(s => suoritusWithDepth((t._1 + 1, s)))
    }

    def suoritusLista: List[Node] = oo.suoritukset.headOption.map(ts =>
        suoritusWithDepth(0, ts).flatMap(s =>
          <tr>
            <td class={"depth-" + s._1}>{s._2.koulutusmoduuli.tunniste.koodiarvo}</td>
            <td class={"depth-" + s._1}>{s._2.koulutusmoduuli.nimi.get("FI")}</td>
            <td>{s._2.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}</td>
            <td>{s._2.arviointi.flatMap(_.lastOption.flatMap(_.päivä.map(_.toString))).getOrElse("")}</td>
          </tr>
        )
      ).toList.flatten

    <html>
      <head>
        <style>
          table {{
            border-collapse: separate;
            border-spacing: 30px 5px;
          }}
          .depth-1 {{ padding-left:0.5em; }}
          .depth-2 {{ padding-left:1em; }}
          .depth-3 {{ padding-left:1.5em; }}
        </style>
      </head>
      <body>
        <h1>Opintosuoritusote</h1>
        <h3>Opintosuoritukset</h3>

        <table>
          <tr>
            <th class="tunnus"></th>
            <th class="nimi"></th>
            <th class="laajuus">Op</th>
            <th class="suoritus-pvm">Suor.pvm</th>
          </tr>
          {
            suoritusLista
          }
        </table>
      </body>
    </html>
  }
}
