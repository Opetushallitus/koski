package fi.oph.koski.email

trait EmailSender {
  def sendEmail(envelope: Email) {}
}

case class Email(email: EmailContent, recipient: List[EmailRecipient])
case class EmailContent(from: String, subject: String, body: String, html: Boolean)
case class EmailRecipient(email: String)

object MockEmailSender extends EmailSender {
  private var mails: List[Email] = Nil
  def sendMail(mail: Email): Unit = this.synchronized {
    mails = mail :: mails
  }
}