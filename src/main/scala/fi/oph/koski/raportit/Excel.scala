package fi.oph.koski.raportit

trait ColumnSetting

case class GroupColumnsWithTitle(
  children: List[(String, Column)]
) extends ColumnSetting

case class Column(
  title: String,
  width: Option[Int] = None,
  comment: Option[String] = None,
  groupingTitle: Option[String] = None
) extends ColumnSetting

object Column {
  def apply(title: String, width: Option[Int] = None, comment: Option[String] = None, groupingTitle: Option[String] = None): Column = {
    val titlePrepended = comment.fold(title)(title + ": " + _)
    new Column(title, width, Some(titlePrepended), groupingTitle)
  }
}

object CompactColumn {
  def apply(title: String, comment: Option[String] = None): Column = Column(title, width = Some(2000), comment = comment)
}

object Columns {
  def flattenGroupingColumns(settings: Seq[(String, ColumnSetting)]): Seq[(String, Column)] = {
    settings.flatMap {
      case (title, g: GroupColumnsWithTitle) => g.children.map { case (s, column) => (s, column.copy(groupingTitle = Some(title))) }
      case (s, c: Column) => List(s -> c)
      case _ => Nil
    }
  }
}

sealed trait Sheet {
  def title: String
}

trait SheetWithColumnSettings extends Sheet {
  def columnSettingsWithIndex: Seq[(Column, Int)]
  def rowIterator(rowIndex: Int): Iterator[Any]
  def rows: Seq[Any]
}

case class DataSheet(title: String, rows: Seq[Product], columnSettings: Seq[(String, Column)]) extends SheetWithColumnSettings {
  def verifyColumnSettingsVsCaseClass(): Unit = {
    // validate that columnSettings matches case class structure
    if (rows.nonEmpty) {
      val namesFromCaseClass = rows.head.getClass.getDeclaredFields.map(_.getName).toList.filterNot(_.startsWith("$"))
      val namesFromSettings = columnSettings.map(_._1)
      if (namesFromCaseClass != namesFromSettings) {
        throw new IllegalArgumentException(s"columnSettings does not match case class: $namesFromCaseClass $namesFromSettings")
      }
    }
  }
  verifyColumnSettingsVsCaseClass()

  override def columnSettingsWithIndex: Seq[(Column, Int)] =
    columnSettings.zipWithIndex.map { case ((x, column), index) => (column, index)}

  override def rowIterator(rowIndex: Int): Iterator[Any] = rows(rowIndex).productIterator
}

case class DynamicDataSheet(title: String, rows: Seq[Seq[Any]], columnSettings: Seq[Column], orderKey: String = "") extends SheetWithColumnSettings {
  override def columnSettingsWithIndex: Seq[(Column, Int)] = columnSettings.zipWithIndex
  override def rowIterator(rowIndex: Int): Iterator[Any] = rows(rowIndex).iterator
}

case class DocumentationSheet(title: String, text: String) extends Sheet

case class WorkbookSettings(title: String, password: Option[String])

