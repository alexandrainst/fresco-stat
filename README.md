<h1>FRESCO-stat</h1>

<p>
  Library for secure numerical computations, statistics and linear algebra on data held by multiple parties without sharing the data.
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#demos">Demos</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About The Project
FRESCO-stat is a library for secure numerical computations, statistics 
and linear algebra using the <a href="https://github.com/aicis/fresco">FRESCO framework</a>.
FRESCO uses secure multi-party computation to enable statistical analysis to be performed on 
datasets distributed among multiple parties without each party seeing the other parties' data.

## Built With
Build the project and the documentation using maven:
```
mvn clean install
mvn javadoc:javadoc
```

<!-- USAGE EXAMPLES -->
## Usage

The functions in the library is in the [core](core)-module and is accessible through three collections of functions:

1. [Statistics](core/src/main/java/dk.alexandra.fresco.stat/Statistics.java) Descriptive statistics, statistical tests and regression.
2. [Linear Algebra](core/src/main/java/dk.alexandra.fresco.stat/LinearAlgebra.java) Advanced linear algebra.
3. [Sampler](core/src/main/java/dk.alexandra.fresco.stat/Sampler.java)  Draw samples from various distributions.

They are used similarly to builders in FRESCO, namely by calling e.g.

```
Statistics.using(builder).chiSquare(observed, expected);
``` 

to perform a ꭓ²-test.

Note that overflows may happen during computation, which will likely appear as very large outputs. 
This may be avoided by normalizing the input data and/or using a bigger modulus in FRESCO.

## Demos

There are a couple of demos in the [demo](demo)-module which is build by running 
```
mvn package
```
in the root directory of the demo-module and then run the desired demo by executing eg.
```
java -jar target/survival-jar-with-dependencies.jar 1
java -jar target/survival-jar-with-dependencies.jar 2
```
in two separate terminals to run the survival analysis demo. 

There are currently three demos. The command line arguments above runs a demo of survival analysis 
using Cox-regression where each party has the data of a patient group, and where the regression 
estimates the difference in death rate between the two groups. 

There is also an example of linear regression on a vertical sharing of a data set of house prices where 
one party knows the price of a house, and the other knows some details about the house such as size
and distance to the nearest metro station. 

The third demo is a demo of extracting a <i>k</i>-anonymous dataset from a distributed dataset. Here, '
the famous adult data set is used. In this demo, one party knows some details about the individuals 
in the data set, and the other knows how much they earn (more or less than $50k). The demo now 
generalized the attributes about individuals and outputs the number of individuals in each income 
bracket for each choice of generalized attributes.  

Each demo should take between 30s to a few minutes to run on a modern laptop.
 
<!-- CONTRIBUTING -->
## Contributing

If you want to help out developing new features for FRESCO-dk.alexandra.fresco.stat or fix a bug you've stumbled upon, it may be done as follows:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/MyFeature`)
3. Commit your Changes (`git commit -m 'Add MyFeature'`)
4. Push to the Branch (`git push origin feature/MyFeature`)
5. Open a Pull Request

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.