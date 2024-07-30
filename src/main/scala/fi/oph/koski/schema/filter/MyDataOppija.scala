package fi.oph.koski.schema.filter

import java.time.LocalDate
import fi.oph.koski.schema.{Opiskeluoikeus, TäydellisetHenkilötiedot}
import fi.oph.scalaschema.annotation._

@Description("Oppija Koski-järjestelmässä. Sisältää henkilötiedot ja listan opiskeluoikeuksista, jotka puolestaan sisältävät suorituksia, läsnäolotietoja jne. Henkilötietoja ei tallenneta Koskeen, vaan haetaan/tallennetaan Opintopolun henkilöpalveluun")
case class MyDataOppija(
  henkilö: TäydellisetHenkilötiedot,
  @Description("Lista henkilön opiskeluoikeuksista. Sisältää vain ne opiskeluoikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja")
  opiskeluoikeudet: Seq[Opiskeluoikeus],
  suostumuksenPaattymispaiva: Option[LocalDate]
)
