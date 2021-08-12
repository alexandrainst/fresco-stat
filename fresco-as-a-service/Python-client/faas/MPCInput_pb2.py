# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: MPCInput.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='MPCInput.proto',
  package='dk.alexandra.fresco.service.messages',
  syntax='proto3',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x0eMPCInput.proto\x12$dk.alexandra.fresco.service.messages\"(\n\x08MPCInput\x12\x12\n\x05value\x18\x01 \x01(\x01H\x00\x88\x01\x01\x42\x08\n\x06_value\"K\n\tMPCVector\x12>\n\x06values\x18\x01 \x03(\x0b\x32..dk.alexandra.fresco.service.messages.MPCInput\"J\n\tMPCMatrix\x12=\n\x04rows\x18\x01 \x03(\x0b\x32/.dk.alexandra.fresco.service.messages.MPCVectorb\x06proto3'
)




_MPCINPUT = _descriptor.Descriptor(
  name='MPCInput',
  full_name='dk.alexandra.fresco.service.messages.MPCInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='value', full_name='dk.alexandra.fresco.service.messages.MPCInput.value', index=0,
      number=1, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='_value', full_name='dk.alexandra.fresco.service.messages.MPCInput._value',
      index=0, containing_type=None,
      create_key=_descriptor._internal_create_key,
    fields=[]),
  ],
  serialized_start=56,
  serialized_end=96,
)


_MPCVECTOR = _descriptor.Descriptor(
  name='MPCVector',
  full_name='dk.alexandra.fresco.service.messages.MPCVector',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='values', full_name='dk.alexandra.fresco.service.messages.MPCVector.values', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=98,
  serialized_end=173,
)


_MPCMATRIX = _descriptor.Descriptor(
  name='MPCMatrix',
  full_name='dk.alexandra.fresco.service.messages.MPCMatrix',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='rows', full_name='dk.alexandra.fresco.service.messages.MPCMatrix.rows', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=175,
  serialized_end=249,
)

_MPCINPUT.oneofs_by_name['_value'].fields.append(
  _MPCINPUT.fields_by_name['value'])
_MPCINPUT.fields_by_name['value'].containing_oneof = _MPCINPUT.oneofs_by_name['_value']
_MPCVECTOR.fields_by_name['values'].message_type = _MPCINPUT
_MPCMATRIX.fields_by_name['rows'].message_type = _MPCVECTOR
DESCRIPTOR.message_types_by_name['MPCInput'] = _MPCINPUT
DESCRIPTOR.message_types_by_name['MPCVector'] = _MPCVECTOR
DESCRIPTOR.message_types_by_name['MPCMatrix'] = _MPCMATRIX
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

MPCInput = _reflection.GeneratedProtocolMessageType('MPCInput', (_message.Message,), {
  'DESCRIPTOR' : _MPCINPUT,
  '__module__' : 'MPCInput_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.MPCInput)
  })
_sym_db.RegisterMessage(MPCInput)

MPCVector = _reflection.GeneratedProtocolMessageType('MPCVector', (_message.Message,), {
  'DESCRIPTOR' : _MPCVECTOR,
  '__module__' : 'MPCInput_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.MPCVector)
  })
_sym_db.RegisterMessage(MPCVector)

MPCMatrix = _reflection.GeneratedProtocolMessageType('MPCMatrix', (_message.Message,), {
  'DESCRIPTOR' : _MPCMATRIX,
  '__module__' : 'MPCInput_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.MPCMatrix)
  })
_sym_db.RegisterMessage(MPCMatrix)


# @@protoc_insertion_point(module_scope)
