package fi.oph.koski.valpas.valpasrepository

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.{skipSyntheticProperties, strictDeserialization}
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, OidOrganisaatio, Organisaatio}
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.koski.valpas.db.ValpasSchema._
import fi.oph.koski.valpas.db._
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeus, ValpasRajapäivätService}
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import fi.oph.scalaschema.{SerializationContext, Serializer}
import org.json4s.JValue
import slick.jdbc.GetResult

import java.time.{LocalDateTime, LocalTime}
import java.util.UUID

class ValpasKuntailmoitusRepository(
  valpasDatabase: ValpasDatabase,
  deserializer: ValidatingAndResolvingExtractor,
  valpasRajapäivätService: ValpasRajapäivätService,
  config: Config
) extends QueryMethods with Logging {

  protected val db: DB = valpasDatabase.db

  private def serialize(model: IlmoitusLisätiedotData): JValue =
    Serializer.serialize(model, SerializationContext(KoskiSchema.schemaFactory))

  private def deserialize(data: JValue): Either[HttpStatus, IlmoitusLisätiedotData] =
    deserializer.extract[IlmoitusLisätiedotData](strictDeserialization)(data)

  private def toDbRows(
    data: ValpasKuntailmoitusLaajatTiedot,
    kontekstiOpiskeluoikeudet: Seq[ValpasOpiskeluoikeus.Oid]
  )(
    tekijäHenkilöOid: String
  ) : Either[HttpStatus, (IlmoitusRow, IlmoitusLisätiedotRow, Seq[IlmoitusOpiskeluoikeusKontekstiRow])] = {
    for {
      tekijäHenkilö <- data.tekijä.henkilö.toRight(
        ValpasErrorCategory.internalError("Tekijähenkilö puuttuu")
      )
      oppijaY <- data.oppijanYhteystiedot.toRight(
        ValpasErrorCategory.internalError("Oppijan yhteystiedot puuttuvat")
      )
      hakenutMuualle <- data.hakenutMuualle.toRight(
        ValpasErrorCategory.internalError("'Hakenut ulkomaille' puuttuu")
      )
      oppijaOid <- data.oppijaOid.toRight(
        ValpasErrorCategory.internalError("Oppijan oid puuttuu")
      )
    } yield {
      val ilmoitus = IlmoitusRow(
        luotu = valpasRajapäivätService.tarkastelupäivä.atTime(LocalTime.now()),
        oppijaOid = oppijaOid,
        kuntaOid = data.kunta.oid,
        tekijäOrganisaatioOid = data.tekijä.organisaatio.oid,
        tekijäOid = tekijäHenkilöOid,
        mitätöity = None
      )
      val lisätiedot = IlmoitusLisätiedotRow(
        ilmoitusUuid = ilmoitus.uuid,
        data = serialize(
          IlmoitusLisätiedotData(
            yhteydenottokieli = data.yhteydenottokieli.map(_.koodiarvo),
            oppijaYhteystiedot = OppijaYhteystiedotData(
              puhelin = oppijaY.puhelinnumero,
              sähköposti = oppijaY.email,
              lähiosoite = oppijaY.lähiosoite,
              postinumero = oppijaY.postinumero,
              postitoimipaikka = oppijaY.postitoimipaikka,
              maa = oppijaY.maa
            ),
            tekijäYhteystiedot = TekijäYhteystiedotData(
              etunimet = tekijäHenkilö.etunimet,
              sukunimi = tekijäHenkilö.sukunimi,
              kutsumanimi = tekijäHenkilö.kutsumanimi,
              puhelin = tekijäHenkilö.puhelinnumero,
              sähköposti = tekijäHenkilö.email
            ),
            tekijäOrganisaatio = data.tekijä.organisaatio,
            kunta = data.kunta,
            hakenutMuualle = hakenutMuualle
          )
        )
      )
      val opiskeluoikeusKontekstiRivit = kontekstiOpiskeluoikeudet.map(opiskeluoikeusOid =>
        IlmoitusOpiskeluoikeusKontekstiRow(
          ilmoitusUuid = ilmoitus.uuid,
          opiskeluoikeusOid = opiskeluoikeusOid
        )
      )
      (ilmoitus, lisätiedot, opiskeluoikeusKontekstiRivit)
    }
  }

  private def fromDbRows(il: IlmoitusRow, lisätiedotRow: Option[IlmoitusLisätiedotRow])
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    lisätiedotRow match {
      case Some(lisätiedot) => fromDbRows(il, lisätiedot)
      case None => Right(fromDbRows(il))
    }
  }

  private def fromDbRows(il: IlmoitusRow, lisätiedotRow: IlmoitusLisätiedotRow)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    for {
      li <- deserialize(lisätiedotRow.data)
    } yield ValpasKuntailmoitusLaajatTiedot(
      oppijaOid = Some(il.oppijaOid),
      id = Some(il.uuid.toString),
      kunta = li.kunta,
      aikaleima = Some(il.luotu),
      tekijä = ValpasKuntailmoituksenTekijäLaajatTiedot(
        organisaatio = li.tekijäOrganisaatio,
        henkilö = Some(ValpasKuntailmoituksenTekijäHenkilö(
          oid = Some(il.tekijäOid),
          etunimet = li.tekijäYhteystiedot.etunimet,
          sukunimi = li.tekijäYhteystiedot.sukunimi,
          kutsumanimi = li.tekijäYhteystiedot.kutsumanimi,
          email = li.tekijäYhteystiedot.sähköposti,
          puhelinnumero = li.tekijäYhteystiedot.puhelin
        ))
      ),
      yhteydenottokieli = li.yhteydenottokieli.map(koodiarvo => Koodistokoodiviite(koodiarvo, "kieli")),
      oppijanYhteystiedot = Some(ValpasKuntailmoituksenOppijanYhteystiedot(
        puhelinnumero = li.oppijaYhteystiedot.puhelin,
        email = li.oppijaYhteystiedot.sähköposti,
        lähiosoite = li.oppijaYhteystiedot.lähiosoite,
        postinumero = li.oppijaYhteystiedot.postinumero,
        postitoimipaikka = li.oppijaYhteystiedot.postitoimipaikka,
        maa = li.oppijaYhteystiedot.maa
      )),
      hakenutMuualle = Some(li.hakenutMuualle),
      onUudempiaIlmoituksiaMuihinKuntiin = None,
      aktiivinen = None
    )
  }

  private def fromDbRows(il: IlmoitusRow)
  : ValpasKuntailmoitusLaajatTiedot = {
    ValpasKuntailmoitusLaajatTiedot(
      oppijaOid = Some(il.oppijaOid),
      id = Some(il.uuid.toString),
      kunta = OidOrganisaatio(oid = il.kuntaOid),
      aikaleima = Some(il.luotu),
      tekijä = ValpasKuntailmoituksenTekijäLaajatTiedot(
        organisaatio = OidOrganisaatio(oid = il.tekijäOrganisaatioOid),
        henkilö = Some(ValpasKuntailmoituksenTekijäHenkilö(
          oid = Some(il.tekijäOid),
          etunimet = None,
          sukunimi = None,
          kutsumanimi = None,
          email = None,
          puhelinnumero = None
        ))
      ),
      yhteydenottokieli = None,
      oppijanYhteystiedot = None,
      hakenutMuualle = None,
      onUudempiaIlmoituksiaMuihinKuntiin = None,
      aktiivinen = None
    )
  }

  def create(model: ValpasKuntailmoitusLaajatTiedot, kontekstiOpiskeluoikeudet: Seq[ValpasOpiskeluoikeus.Oid])
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    model.tekijä.henkilö
      .toRight(ValpasErrorCategory.internalError("tekijä puuttuu"))
      .flatMap(_.oid.toRight(ValpasErrorCategory.internalError("tekijän oid puuttuu")))
      .flatMap(toDbRows(model, kontekstiOpiskeluoikeudet))
      .flatMap { case (ilmoitus: IlmoitusRow, lisätiedot: IlmoitusLisätiedotRow, kontekstiRivit: Seq[IlmoitusOpiskeluoikeusKontekstiRow]) =>
        runDbSync(DBIO.seq(
          Ilmoitukset += ilmoitus,
          IlmoitusLisätiedot += lisätiedot,
          IlmoitusOpiskeluoikeusKonteksti ++= kontekstiRivit
        ).transactionally)
        fromDbRows(ilmoitus, lisätiedot)
      }
  }

  def get(id: UUID): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] =
    for {
      list            <- query(_.uuid === id)
      kuntailmoitus   <- list.headOption.toRight(ValpasErrorCategory.notFound.kuntailmoitustaEiLöydy())
    } yield kuntailmoitus

  def queryOppijat(oppijaOids: Set[String]): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    query(_.oppijaOid inSetBind oppijaOids)
  }

  def queryByKunta(kuntaOid: Organisaatio.Oid): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    query(_.kuntaOid === kuntaOid)
      .map(withUudempiIlmoitusToiseenKuntaan)
  }

  def queryByTekijäOrganisaatio(organisaatioOid: Organisaatio.Oid): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    query(_.tekijäOrganisaatioOid === organisaatioOid)
  }

  private def query[T <: slick.lifted.Rep[_]]
    (filterFn: (IlmoitusTable) => T)
    (implicit wt: slick.lifted.CanBeQueryCondition[T])
  : Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    HttpStatus.foldEithers(
      runDbSync(
        Ilmoitukset
          .filter(_.mitätöity.isEmpty)
          .filter(filterFn)
          .joinLeft(IlmoitusLisätiedot).on(_.uuid === _.ilmoitusUuid)
          .sortBy(_._1.luotu.desc)
          .result
      ).map(Function.tupled(fromDbRows))
    )
  }

  def queryOpiskeluoikeudetWithIlmoitus(opiskeluoikeudet: Seq[String]): Seq[String] = {
    runDbSync(
      IlmoitusOpiskeluoikeusKonteksti
        .filter(_.opiskeluoikeusOid inSetBind opiskeluoikeudet)
        .result)
      .map(_.opiskeluoikeusOid)
  }

  private def withUudempiIlmoitusToiseenKuntaan(ilmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]): Seq[ValpasKuntailmoitusLaajatTiedot] = {
    val ids = ilmoitukset.map(_.id).collect { case Some(id) => id }

    val ilmoituksiaMuualle = queryUudempiaIlmoituksiaMuualla(ids)

    ilmoitukset.map(i => i.copy(
        onUudempiaIlmoituksiaMuihinKuntiin = ilmoituksiaMuualle
          .find(muu => i.id.contains(muu.uuid))
          .map(_.uudempiaIlmoituksiaMuualle)
    ))
  }

  private def queryUudempiaIlmoituksiaMuualla(uuids: Seq[String]): Seq[UudempiaIlmoituksiaMuuallaRow] =
    runDbSync(sql"""
      SELECT
        uuid,
        (
          SELECT COUNT(*) > 0
          FROM ilmoitus AS v
          WHERE
            v.mitätöity IS NULL
            AND v.oppija_oid = ilmoitus.oppija_oid
            AND v.luotu > ilmoitus.luotu
            AND v.kunta_oid <> ilmoitus.kunta_oid
        ) as "uudempia_ilmoituksia_muualle"
      FROM ilmoitus
      WHERE CAST (uuid AS TEXT) = any($uuids);
    """.as[UudempiaIlmoituksiaMuuallaRow])

  private implicit def getResult: GetResult[UudempiaIlmoituksiaMuuallaRow] = GetResult(r => {
    UudempiaIlmoituksiaMuuallaRow(
      uuid = r.rs.getString("uuid"),
      uudempiaIlmoituksiaMuualle = r.rs.getBoolean("uudempia_ilmoituksia_muualle"),
    )
  })

  def queryOpiskeluoikeusKontekstiByIlmoitus(ilmoitusUuid: UUID): Either[HttpStatus, Seq[String]] = {
    queryOpiskeluoikeusKonteksti(_.ilmoitusUuid === ilmoitusUuid)
      .map(_.map(_.opiskeluoikeusOid))
  }

  private def queryOpiskeluoikeusKonteksti[T <: slick.lifted.Rep[_]]
    (filterFn: (IlmoitusOpiskeluoikeusKontekstiTable) => T)
    (implicit wt: slick.lifted.CanBeQueryCondition[T])
  : Either[HttpStatus, Seq[ValpasKuntailmoitusOpiskeluoikeusKonteksti]] = {
    HttpStatus.foldEithers(
      runDbSync(
        IlmoitusOpiskeluoikeusKonteksti
          .filter(filterFn)
          .result
      ).map(k => Right(ValpasKuntailmoitusOpiskeluoikeusKonteksti(k.ilmoitusUuid.toString, k.opiskeluoikeusOid)))
    )
  }

  def deleteLisätiedot(oppijaOid: String) = {
    runDbSync(
      IlmoitusLisätiedot
        .filter(lt => lt.ilmoitusUuid in Ilmoitukset.filter(_.oppijaOid === oppijaOid).map(_.uuid))
        .delete
    )
  }

  def truncate(): Unit = {
    if (Environment.isMockEnvironment(config)) {
      runDbSync(Ilmoitukset.delete)
    } else {
      throw new RuntimeException("Ilmoituksia ei voi tyhjentää tuotantotilassa")
    }
  }
}

case class UudempiaIlmoituksiaMuuallaRow(
  uuid: String,
  uudempiaIlmoituksiaMuualle: Boolean
)
