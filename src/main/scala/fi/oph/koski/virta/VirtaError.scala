package fi.oph.koski.virta

import fi.oph.koski.http.HttpException

object VirtaError {
  // Virran ajoittaiset yksittäiset virheet ovat odotettavissa olevia ulkoisen palvelun yhteys- tai HTTP-virheitä
  // (HttpException ja sen alityypit, esim. 504 timeout). Tällaisesta ei hälytetä error-tasolla. Poikkeus voi olla kääritty
  // (esim. Guavan välimuistin UncheckedExecutionException tai reflektioproxyn poikkeus), joten tarkistetaan koko cause-ketju.
  // Muut poikkeukset (esim. bugi omassa koodissa) eivät ole odotettuja ja ne lokitetaan edelleen error-tasolla.
  def isExpectedFailure(t: Throwable): Boolean =
    Iterator.iterate(Option(t))(_.flatMap(e => Option(e.getCause)))
      .takeWhile(_.isDefined).flatten
      .exists(_.isInstanceOf[HttpException])
}
