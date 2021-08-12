<h1>FRESCO-as-a-service (FaaS)</h1>

<p>
  Service application for using FRESCO-stat from other programming environments.
</p>

<!-- TABLE OF CONTENTS -->
<h2>Table of Contents</h2>

* <a href="#about-the-project">About The Project</a>
* <a href="#build">Build</a>
* <a href="#demos">Demos</a>
* <a href="#contributing">Contributing</a>
* <a href="#license">License</a>
* <a href="#contact">Contact</a>

<!-- ABOUT THE PROJECT -->
## About The Project

FRESCO-stat is a library for secure numerical computations, statistics and
linear algebra using the <a href="https://github.com/aicis/fresco">FRESCO
framework</a>. FRESCO uses secure multi-party computation to enable
computations to be performed on data distributed among multiple parties
without each party seeing the other parties' data. 

The FRESCO-stat library is a Java library, but this project enables it to be used from other enviroments.
Currently, only Python is supported, but any programming language with
support for ZeroMQ and Google Protocol Buffers may use the service.

Currently the following functions are supported:
1. Simple linear regression,
1. Training a multi-layer perceptron,
1. Student <i>t</i>-test,

but any function in FRESCO-stat may easily be included in the service by defining the input and
output as protobuf messages and writing an application calling the appropriate function.
 
## Build
Build the project using Maven:
```
mvn package
```

## Demo

There is a demo in the [demo](demo) folder where we consider a dataset of real
estate pricing in Taipei City, Taiwan (Yeh, I-Cheng and Hsu, Tzu-Kuang
(2018), <i>Building Real Estate Valuation Models with Comparative Approach
through Case-Based Reasoning</i>). The dataset has 414 rows and eight
columns. Each row represents a house and each column an attribute for the
house. We divide the data between two parties such that one knows some
attributes: the house age, distance to the nearest MRT station and number of
convenience stores within walking distance, and the other party knows the
house price per unit area (10000 New Taiwan Dollar/Ping, where Ping is a
local unit with 1 Ping = 3.3m<sup>2</sup>). The two parties want to fit a linear model
on the data where the variables held by the first party are considered to be
the independent variables and the house price is the dependent variable.

To run the demo on a single machine, first build the project as described
above. Then install the Python package by running 

``` pip install . ``` 

in the [Python-client](Python-client) folder. To run the services execute the following
commands from the `service/target` folder in two different terminals 

```
java -jar frescoservice-jar-with-dependencies.jar 1 localhost 5555
java -jar frescoservice-jar-with-dependencies.jar 2 localhost 5556
```

If running on two different machines, replace localhost with the IP-address of
the other machine. Now run the demo by running the [linreg1.py](demo/linreg1.py) and
[linreg2.py](demo/linreg2.py) scripts in two different terminals. After a few minutes, this
the execution of the scripts should end with the output 

```
[ 0.32075882 -0.1007731  -0.3164115   0.11805797]
```
 
<!-- CONTRIBUTING -->
## Contributing

If you want to help out developing new features for FRESCO-stat or fix a bug you've stumbled upon, 
it may be done as follows:

1. Fork the Project
1. Create your Feature Branch (`git checkout -b feature/MyFeature`)
1. Commit your Changes (`git commit -m 'Add MyFeature'`)
1. Push to the Branch (`git push origin feature/MyFeature`)
1. Open a Pull Request

<!-- LICENSE -->
## License

Distributed under the MIT License. See [the licence](LICENSE) for more information.

<!-- CONTACT -->
## Contact 

The library is developed and maintained by the Security Lab at the <a href="https://alexandra.dk/about-the-alexandra-institute/">Alexandra
Institute</a>. If you have found a bug, have questions about the usage of the
library, or you are missing functionality, feel free to contact us at <a href="fresco@alexandra.dk">fresco@alexandra.dk</a>. 