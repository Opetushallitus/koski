package fi.oph.tor.json

import fi.oph.tor.http.HttpStatus
import org.json4s.{Formats, JValue, MappingException}

object ContextualExtractor {
  case class ExtractionState(val context: Any, var status: HttpStatus)

  private val tl = new ThreadLocal[ExtractionState]

  /**
   *  Adds supports for "extraction context" to json4s data extraction (as in json4s ExtractableJsonAstNode).
   *
   *  The context object is provided as a parameter to this method and is available during extraction
   *  by calling the getContext method. Type parameter C is used for extraction context.
   *
   *  Additionally adds support for "extraction errors".
   *  During extraction, the custom (de)serializers may call the `extractionError` method.
   *
   *  This method returns either a successfully extracted object fo type T or a list of errors that were reported
   *  during extraction.
   */
  def extract[T, C](json: JValue, context: C)(implicit mf: Manifest[T], formats: Formats): Either[HttpStatus, T] = {
    val state: ExtractionState = new ExtractionState(context, HttpStatus.ok)
    tl.set(state)

    try {
      val extracted: T = json.extract[T]
      state.status.isOk match {
        case true => Right(extracted)
        case false => Left(state.status)
      }
    } catch {
      case e: MappingException =>
        findResolvingException(e) match {
          case Some(ex) => Left(ex.validationError)
          case _ => throw e
        }
    } finally {
      tl.remove()
    }
  }

  def getContext[T]: Option[T] = threadLocalState.map(_.context.asInstanceOf[T])

  /**
   * Reports extraction error
   */
  def extractionError(error: HttpStatus) = {
    threadLocalState.foreach { state =>
      state.status = HttpStatus.append(state.status, error)
    }
    throw new ExtractionException(error)
  }

  def tryExtract[T](block: => T)(status: => HttpStatus) = {
    try {
      block
    } catch {
      case e: Exception => extractionError(status)
    }
  }

  private def findResolvingException(e: MappingException): Option[ExtractionException] = e.cause match {
    case x:ExtractionException => Some(x)
    case x:MappingException if x != e => findResolvingException(x)
    case _ => None
  }

  private def threadLocalState[T]: Option[ExtractionState] = {
    Option(tl.get)
  }

  private class ExtractionException(val validationError: HttpStatus) extends RuntimeException
}