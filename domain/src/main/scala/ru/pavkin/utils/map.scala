package ru.pavkin.utils

object map {
  def adjust[K, V](m: Map[K, V])(key: K, adjuster: Option[V] ⇒ V): Map[K, V] =
    m.updated(key, adjuster(m.get(key)))

  object syntax {
    implicit class MapUtilsOps[K, V](m: Map[K, V]) {
      def adjust(key: K, adjuster: Option[V] ⇒ V): Map[K, V] =
        ru.pavkin.utils.map.adjust(m)(key, adjuster)
    }
  }
}
