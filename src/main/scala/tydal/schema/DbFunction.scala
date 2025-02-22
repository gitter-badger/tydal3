package tydal.schema

trait DbFunction[+Params <: Tuple, Type] extends Field[Type]:
  def params: Params
  def dbName: String
  override def toString: String = s"$dbName$params"

trait DbFunction1[+F, Type] extends DbFunction[F *: EmptyTuple, Type]:
  def param: F
  def params: F *: EmptyTuple = param *: EmptyTuple

trait DbFunction2[+F, +G, Type] extends DbFunction[(F, G), Type]:
  def param1: F
  def param2: G
  def params: (F, G) = (param1, param2)

trait DbFunction3[+F, +G, +H, Type] extends DbFunction[(F, G, H), Type]:
  def param1: F
  def param2: G
  def param3: H
  def params: (F, G, H) = (param1, param2, param3)

trait Aggregation[+F, Type] extends DbFunction1[F, Type]

final class Avg[T, +F <: Field[T], U](val param: F)(
  using
  rational: Rational[T, U],
  override val dbType: DbType[U]
) extends Aggregation[F, U]:
  override val dbName: String = "AVG"

extension [T, F <: Field[T]](field: F)
  def avg[U](using Rational[T, U], DbType[U]): Avg[T, F, U] = Avg(field)


final class Sum[T: IsNumerical, +F <: Field[T]](val param: F)(
  using
  override val dbType: DbType[T]
) extends Aggregation[F, T]:
  override val dbName: String = "SUM"

extension [T, F <: Field[T]](field: F)
  def sum(using IsNumerical[T], DbType[T]): Sum[T, F] = Sum(field)


final class Count[+F <: Field[_]](val param: F)(
  using
  val dbType: DbType[bigint]
) extends Aggregation[F, bigint]:
  override val dbName: String = "COUNT"

extension [F <: Field[_]](field: F)
  def count: Count[F] = Count(field)


final class Min[T, +F <: Field[T], U, G <: Field[U]](val param: F)(
  using
  nullable: Nullable[F, G],
  override val dbType: DbType[U]
) extends Aggregation[F, U]:
  override val dbName: String = "MIN"

extension [T, F <: Field[T]](field: F)
  def min[U, G <: Field[U]](using Nullable[F, G], DbType[U]): Min[T, F, U, G] = Min(field)


final class Max[T, +F <: Field[T], U, G <: Field[U]](val param: F)(
  using
  nullable: Nullable[F, G],
  override val dbType: DbType[U]
) extends Aggregation[F, U]:
  override val dbName: String = "MAX"

extension [T, F <: Field[T]](field: F)
  def max[U, G <: Field[U]](using Nullable[F, G], DbType[U]): Max[T, F, U, G] = Max(field)


trait Unnested[T, U]

object Unnested:
  given[T]: Unnested[array[T], T] with { }

final class Unnest[T, +F <: Field[T], U](val param: F)(
  using
  unnested: Unnested[T, U],
  override val dbType: DbType[U]
) extends DbFunction1[F, U]:
  override val dbName: String = "UNNEST"

extension [T, F <: Field[T]](field: F)
  def unnest[U](using Unnested[T, U], DbType[U]): Unnest[T, F, U] = Unnest(field)
