package fi.oph.koski.valpas.valpasrepository

import com.typesafe.config.Config
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.{skipSyntheticProperties, strictDeserialization}
import fi.oph.koski.schema.{Koodistokoodiviite, KoskiSchema, OidOrganisaatio, Organisaatio}
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.db.ValpasSchema._
import fi.oph.koski.valpas.db._
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.scalaschema.{SerializationContext, Serializer}
import org.json4s.JValue

import java.time.LocalTime

class ValpasKuntailmoitusRepository(
  valpasDatabase: ValpasDatabase,
  deserializer: ValidatingAndResolvingExtractor,
  valpasRajapäivätService: ValpasRajapäivätService,
  config: Config
) extends QueryMethods with Logging {

  protected val db: DB = valpasDatabase.db

  private def serialize(model: IlmoitusLisätiedotData): JValue =
    Serializer.serialize(model, SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties))

  private def deserialize(data: JValue): Either[HttpStatus, IlmoitusLisätiedotData] =
    deserializer.extract[IlmoitusLisätiedotData](strictDeserialization)(data)

  private def toDbRows(data: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)(tekijäHenkilöOid: String)
  : Either[HttpStatus, (IlmoitusRow, IlmoitusLisätiedotRow)] = {
    for {
      tekijäHenkilö <- data.kuntailmoitus.tekijä.henkilö.toRight(
        ValpasErrorCategory.internalError("Tekijähenkilö puuttuu")
      )
      oppijaY <- data.kuntailmoitus.oppijanYhteystiedot.toRight(
        ValpasErrorCategory.internalError("Oppijan yhteystiedot puuttuvat")
      )
      hakenutMuualle <- data.kuntailmoitus.hakenutMuualle.toRight(
        ValpasErrorCategory.internalError("'Hakenut ulkomaille' puuttuu")
      )
    } yield {
      val ilmoitus = IlmoitusRow(
        luotu = valpasRajapäivätService.tarkastelupäivä.atTime(LocalTime.now()),
        oppijaOid = data.oppijaOid,
        kuntaOid = data.kuntailmoitus.kunta.oid,
        tekijäOrganisaatioOid = data.kuntailmoitus.tekijä.organisaatio.oid,
        tekijäOid = tekijäHenkilöOid
      )
      val lisätiedot = IlmoitusLisätiedotRow(
        ilmoitusUuid = ilmoitus.uuid,
        data = serialize(
          IlmoitusLisätiedotData(
            yhteydenottokieli = data.kuntailmoitus.yhteydenottokieli.map(_.koodiarvo),
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
            tekijäOrganisaatio = data.kuntailmoitus.tekijä.organisaatio,
            kunta = data.kuntailmoitus.kunta,
            hakenutMuualle = hakenutMuualle
          )
        )
      )
      (ilmoitus, lisätiedot)
    }
  }

  private def fromDbRows(il: IlmoitusRow, lisätiedotRow: Option[IlmoitusLisätiedotRow])
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    lisätiedotRow match {
      case Some(lisätiedot) => fromDbRows(il, lisätiedot)
      case None => Right(fromDbRows(il))
    }
  }

  private def fromDbRows(il: IlmoitusRow, lisätiedotRow: IlmoitusLisätiedotRow)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    for {
      li <- deserialize(lisätiedotRow.data)
    } yield ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid = il.oppijaOid,
      kuntailmoitus = ValpasKuntailmoitusLaajatTiedot(
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
        hakenutMuualle = Some(li.hakenutMuualle)
      )
    )
  }

  private def fromDbRows(il: IlmoitusRow)
  : ValpasKuntailmoitusLaajatTiedotJaOppijaOid = {
    ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
      oppijaOid = il.oppijaOid,
      kuntailmoitus = ValpasKuntailmoitusLaajatTiedot(
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
      )
    )
  }

  def create(model: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    model.kuntailmoitus.tekijä.henkilö
      .toRight(ValpasErrorCategory.internalError("tekijä puuttuu"))
      .flatMap(_.oid.toRight(ValpasErrorCategory.internalError("tekijän oid puuttuu")))
      .flatMap(toDbRows(model))
      .flatMap { case (ilmoitus: IlmoitusRow, lisätiedot: IlmoitusLisätiedotRow) =>
        runDbSync(DBIO.seq(
          Ilmoitukset += ilmoitus,
          IlmoitusLisätiedot += lisätiedot
        ).transactionally)
        fromDbRows(ilmoitus, lisätiedot)
      }
  }

  def queryOppijat(oppijaOids: Set[String]): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedot]] = {
    query(_.oppijaOid inSetBind oppijaOids)
      .map(_.map(_.kuntailmoitus))
  }

  def queryByKunta(kuntaOid: Organisaatio.Oid): Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedotJaOppijaOid]] = {
    query(_.kuntaOid === kuntaOid)
  }

  def query[T <: slick.lifted.Rep[_]]
    (filterFn: (IlmoitusTable) => T)
    (implicit wt : slick.lifted.CanBeQueryCondition[T])
  : Either[HttpStatus, Seq[ValpasKuntailmoitusLaajatTiedotJaOppijaOid]] = {
    HttpStatus.foldEithers(
      runDbSync(
        Ilmoitukset
          .filter(filterFn)
          .joinLeft(IlmoitusLisätiedot).on(_.uuid === _.ilmoitusUuid)
          .sortBy(_._1.luotu.desc)
          .result
      ).map(Function.tupled(fromDbRows))
    )
  }

  def truncate(): Unit = {
    if (config.getString("opintopolku.virkailija.url") == "mock") {
      runDbSync(Ilmoitukset.delete)
    }
  }
}
