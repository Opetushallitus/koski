package fi.oph.tor.schema

import fi.oph.tor.schema.generic.annotation._

@Description("Oppija Koski-järjestelmässä. Sisältää henkilötiedot ja listan opiskeluoikeuksista, jotka puolestaan sisältävät suorituksia, läsnäolotietoja jne. Henkilötietoja ei tallenneta Koskeen, vaan haetaan/tallennetaan Opintopolun henkilöpalveluun.")
case class Oppija(
  henkilö: Henkilö,
  @Description("Lista henkilön opiskeluoikeuksista. Sisältää vain ne opiskeluoikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja")
  opiskeluoikeudet: Seq[Opiskeluoikeus]
)