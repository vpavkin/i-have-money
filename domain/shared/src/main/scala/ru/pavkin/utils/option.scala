package ru.pavkin.utils

object option {
  def notEmpty(s: String): Option[String] = if (s.nonEmpty) Some(s) else None
}
