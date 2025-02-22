package tydal.schema

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID
import skunk.Codec
import skunk.codec.{all => codecs}
import skunk.data.{Type => TypeName}

trait DbType[T]:
  type Out
  def codec: Codec[Out]
  def dbName: String

type char
type varchar
type text
type smallint
type integer
type bigint
type float4
type float8
type numeric
type bool
type uuid
type date
type timestamp
type array[T]
type json[T]  // todo: encoding / decoding
type nullable[T]
type `enum`[Name, T]

trait Enumerated[T]:
  def toString(t: T): String
  def fromString(s: String): Option[T]

object DbType:
  type Aux[T, U] = DbType[T] { type Out = U }

  given DbType[char] with
    type Out = String
    def codec: Codec[String] = codecs.varchar   // no codecs for char?
    def dbName: String = "char"

  given DbType[varchar] with
    type Out = String
    def codec: Codec[String] = codecs.varchar
    def dbName: String = "varchar"

  given DbType[text] with
    type Out = String
    def codec: Codec[String] = codecs.text
    def dbName: String = "text"

  given DbType[smallint] with
    type Out = Short
    def codec: Codec[Short] = codecs.int2
    def dbName: String = "smallint"

  given DbType[integer] with
    type Out = Int
    def codec: Codec[Int] = codecs.int4
    def dbName: String = "integer"

  given DbType[bigint] with
    type Out = Long
    def codec: Codec[Long] = codecs.int8
    def dbName: String = "bigint"

  given DbType[float4] with
    type Out = Float
    def codec: Codec[Float] = codecs.float4
    def dbName: String = "float4"

  given DbType[float8] with
    type Out = Double
    def codec: Codec[Double] = codecs.float8
    def dbName: String = "float8"

  given DbType[numeric] with
    type Out = BigDecimal
    def codec: Codec[BigDecimal] = codecs.numeric
    def dbName: String = "numeric"

  given DbType[bool] with
    type Out = Boolean
    def codec: Codec[Boolean] = codecs.bool
    def dbName: String = "bool"

  given DbType[uuid] with
    type Out = UUID
    def codec: Codec[UUID] = codecs.uuid
    def dbName: String = "uuid"

  given DbType[date] with
    type Out = LocalDate
    def codec: Codec[LocalDate] = codecs.date
    def dbName: String = "date"

  given DbType[timestamp] with
    type Out = Instant
    def codec: Codec[Instant] = codecs.timestamp.imap(_.atZone(ZoneOffset.UTC).toInstant)(i => LocalDateTime.ofInstant(i, ZoneOffset.UTC))
    override def dbName: String = "timestamp"

  given[T](using innerType: DbType[T]): DbType[array[T]] with
    type Out = Seq[innerType.Out]
    def codec: Codec[Seq[innerType.Out]] = ???  // todo: does skunk really support arrays?
    def dbName: String = s"${innerType.dbName}[]"

  // todo: using dependent type here makes DecoderAdapter fail for Scala 3.0.0
  given[T: IsNotNullable, U](using innerType: DbType.Aux[T, U]): DbType[nullable[T]] with
    type Out = Option[U]
    def codec: Codec[Option[U]] = innerType.codec.opt
    def dbName: String = innerType.dbName

  given[Name <: String, T] (using singleton: ValueOf[Name], enumerated: Enumerated[T]): DbType[`enum`[Name, T]] with
    type Out = T
    def codec: Codec[T] = codecs.`enum`[T](enumerated.toString, enumerated.fromString, TypeName(singleton.value))
    def dbName: String = singleton.value
