import zmq
import numpy

from .LinearRegression_pb2 import LinearRegressionInput
from .NeuralNetwork_pb2 import NeuralNetworkInput
from .MPCInput_pb2 import MPCInput, MPCVector, MPCMatrix
from .ServiceIO_pb2 import ServiceInput, ServiceOutput

def connect(port):
    context = zmq.Context()
    socket = context.socket(zmq.REQ)
    socket.connect("tcp://localhost:" + str(port))
    return Faas(socket)

class Faas:
    def __init__(self, socket = None):
        self.socket = socket

    def linreg(self, x, y):
        myInput = LinearRegressionInput()
        
        for yValue in y:
            yi = MPCInput()
            if yValue != None:
                yi.value = yValue
            myInput.y.values.append(yi)

        for xRow in x:
            v = MPCVector()
            for xValue in xRow:
                xi = MPCInput()
                if xValue != None:
                    xi.value = xValue
                v.values.append(xi)
            myInput.x.append(v)      
        
        serviceInput = ServiceInput()
        serviceInput.linearRegressionInput.CopyFrom(myInput)
        serialized = serviceInput.SerializeToString()
       
        self.socket.send(serialized)
        received = self.socket.recv()
        result = ServiceOutput.FromString(received)
        resultAsArray = numpy.fromiter(result.linearRegressionOutput.beta, dtype=float)
        
        return resultAsArray
    
    def neuralnetwork(self, x, y, weights, biases, 
                      categories = 2, epochs = 1, learningrate = 1.0):
        
        myInput = NeuralNetworkInput()
        
        for yRow in y:
            v = MPCVector()
            if isinstance(yRow, numpy.ndarray):
                for yValue in yRow:
                    yi = MPCInput()
                    if yValue != None:
                        yi.value = yValue
                    v.values.append(yi)
            else:
                yi = MPCInput()
                if yRow != None:
                    yi.value = yRow
                v.values.append(yi)
            myInput.data.y.append(v)
            
        for xRow in x:
            v = MPCVector()
            for xValue in xRow:
                xi = MPCInput()
                if xValue != None:
                    xi.value = xValue
                v.values.append(xi)
            myInput.data.x.append(v)
            
        for w in weights:
            m = MPCMatrix()
            for row in w:
                v = MPCVector()
                if isinstance(row, numpy.ndarray):
                    for xValue in row:
                        xi = MPCInput()
                        if xValue != None:
                            xi.value = xValue
                        v.values.append(xi)
                else:
                    xi = MPCInput()
                    if row != None:
                        xi.value = row
                    v.values.append(xi)                    
                m.rows.append(v)
            myInput.network.weights.append(m)
            
        for b in biases:
            v = MPCVector()
            if isinstance(b, numpy.ndarray):
                for xValue in b:
                    xi = MPCInput()
                    if xValue != None:
                        xi.value = xValue
                    v.values.append(xi)
            else:
                xi = MPCInput()
                if b != None:
                    xi.value = b
                v.values.append(xi)
            myInput.network.biases.append(v)
            
        myInput.categories = categories
        myInput.epochs = epochs
        myInput.learningrate = learningrate

        serviceInput = ServiceInput()
        serviceInput.neuralNetworkInput.CopyFrom(myInput)
        serialized = serviceInput.SerializeToString()
       
        self.socket.send(serialized)
        received = self.socket.recv()
        result = ServiceOutput.FromString(received)
        
        return result.neuralNetworkOutput
        