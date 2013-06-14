{{#public}}
package {{package}}

import com.twitter.scrooge.{
  ThriftException, ThriftStruct, ThriftStructCodec3, ThriftUtil}
import org.apache.thrift.protocol._
import org.apache.thrift.transport.TMemoryBuffer
import java.nio.ByteBuffer
{{#withFinagleClient}}
import com.twitter.finagle.SourcedException
{{/withFinagleClient}}
import scala.collection.mutable
import scala.collection.{Map, Set}

{{/public}}
{{docstring}}
object {{StructName}} extends ThriftStructCodec3[{{StructName}}] {
  val Struct = new TStruct("{{StructNameForWire}}")
{{#fields}}
  val {{fieldConst}} = new TField("{{fieldNameForWire}}", TType.{{constType}}, {{id}})
{{/fields}}

  /**
   * Checks that all required fields are non-null.
   */
  def validate(_item: {{StructName}}) {
{{#fields}}
{{#required}}
{{#nullable}}
    if (_item.{{fieldName}} == null) throw new TProtocolException("Required field {{fieldName}} cannot be null")
{{/nullable}}
{{/required}}
{{/fields}}
  }

  override def encode(_item: {{StructName}}, _oproto: TProtocol) { _item.write(_oproto) }
  override def decode(_iprot: TProtocol): {{StructName}} = Immutable.decode(_iprot)

  def apply(
{{#fields}}
    {{fieldName}}: {{>optionalType}}{{#hasDefaultValue}} = {{defaultFieldValue}}{{/hasDefaultValue}}{{#optional}} = None{{/optional}}
{{/fields|,}}
  ): {{StructName}} = new Immutable(
{{#fields}}
    {{fieldName}}
{{/fields|,}}
  )

{{#arity0}}
  def unapply(_item: {{StructName}}): Boolean = true
{{/arity0}}
{{#arity1}}
  def unapply(_item: {{StructName}}): Option[{{>optionalType}}] = Some(_item.{{fieldName}})
{{/arity1}}
{{#arityN}}
  def unapply(_item: {{StructName}}): Option[{{product}}] = Some(_item)
{{/arityN}}

  object Immutable extends ThriftStructCodec3[{{StructName}}] {
    override def encode(_item: {{StructName}}, _oproto: TProtocol) { _item.write(_oproto) }
    override def decode(_iprot: TProtocol): {{StructName}} = {
{{#enablePassthrough}}
      var _passthroughs = ThriftUtil.EmptyPassthroughs
{{/enablePassthrough}}
{{#fields}}
      var {{fieldName}}: {{fieldType}} = {{defaultReadValue}}
      var {{gotName}} = false
{{/fields}}
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
{{#fields}}
{{#readWriteInfo}}
            {{>readField}}
{{/readWriteInfo}}
{{/fields}}
            case _ =>
{{#enablePassthrough}}
              val _pass_buff = new TMemoryBuffer(32)
              val _pass_prot = new TCompactProtocol(_pass_buff)
              ThriftUtil.transfer(_pass_prot, _iprot, _field.`type`)
              _passthroughs += (_field -> _pass_buff)
{{/enablePassthrough}}
{{^enablePassthrough}}
              TProtocolUtil.skip(_iprot, _field.`type`)
{{/enablePassthrough}}
          }
          _iprot.readFieldEnd()
        }
      }
      _iprot.readStructEnd()
{{#fields}}
{{#required}}
      if (!{{gotName}}) throw new TProtocolException("Required field '{{StructNameForWire}}' was not found in serialized data for struct {{StructName}}")
{{/required}}
{{/fields}}
      val obj = new Immutable(
{{#fields}}
{{#optional}}
        if ({{gotName}}) Some({{fieldName}}) else None
{{/optional}}
{{^optional}}
        {{fieldName}}
{{/optional}}
{{/fields|,}}
      )
{{#enablePassthrough}}
      obj.__passthrough_fields = _passthroughs
{{/enablePassthrough}}
      obj
    }
  }

  /**
   * The default read-only implementation of {{StructName}}.  You typically should not need to
   * directly reference this class; instead, use the {{StructName}}.apply method to construct
   * new instances.
   */
  class Immutable(
{{#fields}}
    val {{fieldName}}: {{>optionalType}}{{#hasDefaultValue}} = {{defaultFieldValue}}{{/hasDefaultValue}}{{#optional}} = None{{/optional}}
{{/fields|,}}
  ) extends {{StructName}}

{{#withProxy}}
  /**
   * This Proxy trait allows you to extend the {{StructName}} trait with additional state or
   * behavior and implement the read-only methods from {{StructName}} using an underlying
   * instance.
   */
  trait Proxy extends {{StructName}} {
    protected def {{underlyingStructName}}: {{StructName}}
{{#enablePassthrough}}
    override def _passthrough_fields = {{underlyingStructName}}._passthrough_fields
{{/enablePassthrough}}
{{#fields}}
    def {{fieldName}}: {{>optionalType}} = {{underlyingStructName}}.{{fieldName}}
{{/fields}}
  }
{{/withProxy}}
}

trait {{StructName}} extends {{parentType}}
  with {{product}}
  with java.io.Serializable
{
  import {{StructName}}._

{{#enablePassthrough}}
  private[{{StructName}}] var __passthrough_fields = ThriftUtil.EmptyPassthroughs
  def _passthrough_fields = __passthrough_fields

  def withoutPassthroughs(f: TField => Boolean) = {
    val obj = copy()
    obj.__passthrough_fields = _passthrough_fields.filterNot { case (field, _) => f(field) }
    obj
  }
{{/enablePassthrough}}
{{^enablePassthrough}}
  def withoutPassthroughs(f: TField => Boolean) = this
{{/enablePassthrough}}

{{#fields}}
  def {{fieldName}}: {{>optionalType}}
{{/fields}}

{{#fields}}
  def _{{indexP1}} = {{fieldName}}
{{/fields}}

  override def write(_oprot: TProtocol) {
    {{StructName}}.validate(this)
    _oprot.writeStructBegin(Struct)
{{#fields}}
{{#readWriteInfo}}
    {{>writeField}}
{{/readWriteInfo}}
{{/fields}}
{{#enablePassthrough}}
    _passthrough_fields foreach { case (field, buff) =>
      _oprot.writeFieldBegin(field)
      val prot = new TCompactProtocol(buff)
      ThriftUtil.transfer(_oprot, prot, field.`type`)
    }
{{/enablePassthrough}}
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
{{#fields}}
    {{fieldName}}: {{>optionalType}} = this.{{fieldName}}
{{/fields|, }}
  ): {{StructName}} = {
    val obj = new Immutable(
{{#fields}}
      {{fieldName}}
{{/fields|, }}
    )
{{#enablePassthrough}}
    obj.__passthrough_fields = this._passthrough_fields
{{/enablePassthrough}}
    obj
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[{{StructName}}]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

{{#hasExceptionMessage}}
  override def getMessage: String = String.valueOf({{exceptionMessageField}})
{{/hasExceptionMessage}}

  override def productArity: Int = {{arity}}

  override def productElement(n: Int): Any = n match {
{{#fields}}
    case {{index}} => {{fieldName}}
{{/fields}}
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix: String = "{{StructName}}"
}
