# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: NeuralNetwork.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from . import MPCInput_pb2 as MPCInput__pb2
from . import Common_pb2 as Common__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='NeuralNetwork.proto',
  package='dk.alexandra.fresco.service.messages',
  syntax='proto3',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x13NeuralNetwork.proto\x12$dk.alexandra.fresco.service.messages\x1a\x0eMPCInput.proto\x1a\x0c\x43ommon.proto\"\x9c\x01\n\x17NeuralNetworkParameters\x12@\n\x07weights\x18\x01 \x03(\x0b\x32/.dk.alexandra.fresco.service.messages.MPCMatrix\x12?\n\x06\x62iases\x18\x02 \x03(\x0b\x32/.dk.alexandra.fresco.service.messages.MPCVector\"\x8b\x01\n\x11NeuralNetworkData\x12:\n\x01x\x18\x01 \x03(\x0b\x32/.dk.alexandra.fresco.service.messages.MPCVector\x12:\n\x01y\x18\x02 \x03(\x0b\x32/.dk.alexandra.fresco.service.messages.MPCVector\"\xe5\x01\n\x12NeuralNetworkInput\x12N\n\x07network\x18\x01 \x01(\x0b\x32=.dk.alexandra.fresco.service.messages.NeuralNetworkParameters\x12\x45\n\x04\x64\x61ta\x18\x02 \x01(\x0b\x32\x37.dk.alexandra.fresco.service.messages.NeuralNetworkData\x12\x12\n\ncategories\x18\x03 \x01(\x05\x12\x0e\n\x06\x65pochs\x18\x04 \x01(\x05\x12\x14\n\x0clearningrate\x18\x05 \x01(\x01\"\x92\x01\n\x13NeuralNetworkOutput\x12=\n\x07weights\x18\x01 \x03(\x0b\x32,.dk.alexandra.fresco.service.messages.Matrix\x12<\n\x06\x62iases\x18\x02 \x03(\x0b\x32,.dk.alexandra.fresco.service.messages.Vector*!\n\x12\x41\x63tivationFunction\x12\x0b\n\x07SIGMOID\x10\x00\x62\x06proto3'
  ,
  dependencies=[MPCInput__pb2.DESCRIPTOR,Common__pb2.DESCRIPTOR,])

_ACTIVATIONFUNCTION = _descriptor.EnumDescriptor(
  name='ActivationFunction',
  full_name='dk.alexandra.fresco.service.messages.ActivationFunction',
  filename=None,
  file=DESCRIPTOR,
  create_key=_descriptor._internal_create_key,
  values=[
    _descriptor.EnumValueDescriptor(
      name='SIGMOID', index=0, number=0,
      serialized_options=None,
      type=None,
      create_key=_descriptor._internal_create_key),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=773,
  serialized_end=806,
)
_sym_db.RegisterEnumDescriptor(_ACTIVATIONFUNCTION)

ActivationFunction = enum_type_wrapper.EnumTypeWrapper(_ACTIVATIONFUNCTION)
SIGMOID = 0



_NEURALNETWORKPARAMETERS = _descriptor.Descriptor(
  name='NeuralNetworkParameters',
  full_name='dk.alexandra.fresco.service.messages.NeuralNetworkParameters',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='weights', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkParameters.weights', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='biases', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkParameters.biases', index=1,
      number=2, type=11, cpp_type=10, label=3,
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
  serialized_start=92,
  serialized_end=248,
)


_NEURALNETWORKDATA = _descriptor.Descriptor(
  name='NeuralNetworkData',
  full_name='dk.alexandra.fresco.service.messages.NeuralNetworkData',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='x', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkData.x', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='y', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkData.y', index=1,
      number=2, type=11, cpp_type=10, label=3,
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
  serialized_start=251,
  serialized_end=390,
)


_NEURALNETWORKINPUT = _descriptor.Descriptor(
  name='NeuralNetworkInput',
  full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='network', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput.network', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='data', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput.data', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='categories', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput.categories', index=2,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='epochs', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput.epochs', index=3,
      number=4, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='learningrate', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkInput.learningrate', index=4,
      number=5, type=1, cpp_type=5, label=1,
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
  ],
  serialized_start=393,
  serialized_end=622,
)


_NEURALNETWORKOUTPUT = _descriptor.Descriptor(
  name='NeuralNetworkOutput',
  full_name='dk.alexandra.fresco.service.messages.NeuralNetworkOutput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='weights', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkOutput.weights', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='biases', full_name='dk.alexandra.fresco.service.messages.NeuralNetworkOutput.biases', index=1,
      number=2, type=11, cpp_type=10, label=3,
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
  serialized_start=625,
  serialized_end=771,
)

_NEURALNETWORKPARAMETERS.fields_by_name['weights'].message_type = MPCInput__pb2._MPCMATRIX
_NEURALNETWORKPARAMETERS.fields_by_name['biases'].message_type = MPCInput__pb2._MPCVECTOR
_NEURALNETWORKDATA.fields_by_name['x'].message_type = MPCInput__pb2._MPCVECTOR
_NEURALNETWORKDATA.fields_by_name['y'].message_type = MPCInput__pb2._MPCVECTOR
_NEURALNETWORKINPUT.fields_by_name['network'].message_type = _NEURALNETWORKPARAMETERS
_NEURALNETWORKINPUT.fields_by_name['data'].message_type = _NEURALNETWORKDATA
_NEURALNETWORKOUTPUT.fields_by_name['weights'].message_type = Common__pb2._MATRIX
_NEURALNETWORKOUTPUT.fields_by_name['biases'].message_type = Common__pb2._VECTOR
DESCRIPTOR.message_types_by_name['NeuralNetworkParameters'] = _NEURALNETWORKPARAMETERS
DESCRIPTOR.message_types_by_name['NeuralNetworkData'] = _NEURALNETWORKDATA
DESCRIPTOR.message_types_by_name['NeuralNetworkInput'] = _NEURALNETWORKINPUT
DESCRIPTOR.message_types_by_name['NeuralNetworkOutput'] = _NEURALNETWORKOUTPUT
DESCRIPTOR.enum_types_by_name['ActivationFunction'] = _ACTIVATIONFUNCTION
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

NeuralNetworkParameters = _reflection.GeneratedProtocolMessageType('NeuralNetworkParameters', (_message.Message,), {
  'DESCRIPTOR' : _NEURALNETWORKPARAMETERS,
  '__module__' : 'NeuralNetwork_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.NeuralNetworkParameters)
  })
_sym_db.RegisterMessage(NeuralNetworkParameters)

NeuralNetworkData = _reflection.GeneratedProtocolMessageType('NeuralNetworkData', (_message.Message,), {
  'DESCRIPTOR' : _NEURALNETWORKDATA,
  '__module__' : 'NeuralNetwork_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.NeuralNetworkData)
  })
_sym_db.RegisterMessage(NeuralNetworkData)

NeuralNetworkInput = _reflection.GeneratedProtocolMessageType('NeuralNetworkInput', (_message.Message,), {
  'DESCRIPTOR' : _NEURALNETWORKINPUT,
  '__module__' : 'NeuralNetwork_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.NeuralNetworkInput)
  })
_sym_db.RegisterMessage(NeuralNetworkInput)

NeuralNetworkOutput = _reflection.GeneratedProtocolMessageType('NeuralNetworkOutput', (_message.Message,), {
  'DESCRIPTOR' : _NEURALNETWORKOUTPUT,
  '__module__' : 'NeuralNetwork_pb2'
  # @@protoc_insertion_point(class_scope:dk.alexandra.fresco.service.messages.NeuralNetworkOutput)
  })
_sym_db.RegisterMessage(NeuralNetworkOutput)


# @@protoc_insertion_point(module_scope)
