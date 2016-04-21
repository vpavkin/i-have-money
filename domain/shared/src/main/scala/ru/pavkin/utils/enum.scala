package ru.pavkin.utils

import shapeless.{:+:, CNil, Coproduct, Generic, Witness}
import scala.language.implicitConversions

object enum {

  object Values {
    implicit def conv[T](self: this.type)(implicit v: MkValues[T]): Set[T] = Values[T]

    def apply[T](implicit v: MkValues[T]): Set[T] = v.values.toSet

    trait MkValues[T] {
      def values: List[T]
    }

    object MkValues {
      implicit def values[T, Repr <: Coproduct]
      (implicit gen: Generic.Aux[T, Repr], v: Aux[T, Repr]): MkValues[T] =
        new MkValues[T] {
          def values = v.values
        }

      trait Aux[T, Repr] {
        def values: List[T]
      }

      object Aux {
        implicit def cnilAux[A]: Aux[A, CNil] =
          new Aux[A, CNil] {
            def values = Nil
          }

        implicit def cconsAux[T, L <: T, R <: Coproduct]
        (implicit l: Witness.Aux[L], r: Aux[T, R]): Aux[T, L :+: R] =
          new Aux[T, L :+: R] {
            def values = l.value :: r.values
          }
      }
    }
  }

}
