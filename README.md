[![Maven Central](https://img.shields.io/maven-central/v/dk.alexandra.fresco/stat.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22dk.alexandra.fresco%22%20AND%20a:%22stat%22) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/alexandrainst/fresco-stat/blob/master/README.md) 

<h1>FRESCO-stat</h1>

<p>
  Library for secure statistics and linear algebra on data held by multiple parties without sharing 
  the data with the other parties.
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
FRESCO-stat is a library for secure numerical computations, statistics 
and linear algebra using the <a href="https://github.com/aicis/fresco">FRESCO framework</a>.
FRESCO uses secure multi-party computation to enable computations to be performed on 
data distributed among multiple parties without each party seeing the other parties' data.

There is no assumption on how the data is distributed among the parties -- the data may 
for example be divided horizontally, where each party has a different set of data entries, 
or vertically, where each party has different attributes for the same entries, or any combination 
of the two. Or one party could know the model parameters and other parties the data used to fit the,
model eg. in regression or machine learning.

The library has four function libraries with the following functions:
1. [Statistics](src/main/java/dk/alexandra/fresco/stat/Statistics.java)
    * Descriptive statistics
        * Sample mean
        * Sample standard deviation
        * Sample variance
        * Sample median
        * Simple and multi-dimensional histograms
        * Frequency table
        * Sample correlation
        * Sample percentiles
    * Regression
        * Simple and multivariate linear regression
    * Statistical tests
        * One- and two-sample t-test
        * ꭓ²-test
        * F-test
        * Kruskall-Wallis test
    * Survival analysis
        * Cox regression
    * Anonymization methods
        * <i>k</i>-anonymization
        * Differentially private histograms
        * Differentially private linear regression
1. [Sampling from various distributions](src/main/java/dk/alexandra/fresco/stat/Sampler.java)
    * Bernoulli distribution
    * Categorical distribution
    * Exponential distribution
    * Irwin-Hall distribution
    * Laplace distribution
    * Normal distribution
    * Rademacher distribution
    * Uniform distribution
1. [Linear Algebra](src/main/java/dk/alexandra/fresco/stat/AdvancedLinearAlgebra.java) (see also the <i>fixed</i> library in FRESCO for basic linear algebra functions)
    * Back- and forward substitution
    * Gram-Schmidt process
    * Inverse of triangular matrices
    * Moore-Penrose pseudo inverse
    * QR-algorithm for eigenvalue computation
    * QR-decomposition
1. [Machine learning](core/src/main/java/dk/alexandra/fresco/stat/MachineLearning.java)
    * Logistic regression via gradient descent
    * Multilayer perceptron 

They are used similarly to computation directories in FRESCO by calling e.g.

```
Statistics.using(builder).chiSquare(observed, expected);
``` 

to perform a ꭓ²-test on a dataset.

Note that it is not uncommon to experience overflows during computation, which will appear as very large 
outputs. This may be avoided by normalizing the input data and/or using a bigger modulus in FRESCO.

## Build
Build the project and the documentation using Maven:
```
mvn clean install
mvn javadoc:javadoc
```
The javadoc can be found in the `target/apidocs` folder.

The library may be included in other projects by including it as a Maven dependency.
```
<dependency>
  <artifactId>stat</artifactId>
  <groupId>dk.alexandra.fresco</groupId>
  <version>0.2</version>
</dependency>
```

## Demos

There are a couple of demos in the [demo](demo)-module which is build by running 
```
mvn package
```
in the root directory of the [demo](demo) folder. To run a demo, execute eg.
```
java -jar target/survival-jar-with-dependencies.jar 1 localhost
java -jar target/survival-jar-with-dependencies.jar 2 localhost
```
in two separate terminals and change the name of the jar to the desired demo. If running on two 
different machines, replace localhost with the IP-address of the other machine.

There are currently three demos. The command line arguments above runs a demo of survival analysis 
using Cox-regression where each party has the data of a patient group, and where the regression 
estimates the difference in death rate between the two groups. This dataset is taken from 
<i>Statistical methods in medical research</i> by P. Armitage, G. Berry, J.N.S. Matthews. 

There is also an example of linear regression on a vertical sharing of a data set of average masses for women as a function
of their height in a sample of American women of age 30–39 taken from an example from 
<a href="https://en.wikipedia.org/wiki/Simple_linear_regression">Wikipedia</a>.

The third demo is a demo of extracting a <i>k</i>-anonymous dataset from a distributed dataset. Here, '
the <a href="https://archive.ics.uci.edu/ml/datasets/adult">adult</a> dataset is used. In this demo, 
one party knows some details about the individuals in the data set, and the other knows how much they 
earn (more or less than $50k). The demo now generalizes the attributes about individuals and outputs 
the number of individuals in each income bracket for each choice of generalized attributes.  

Each demo should take between 30s to a few minutes to run with both instances on the same machine, 
and a bit longer if running on two machines over a LAN or the internet.  
 
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
The library is developed and maintained by the Security Lab at the <a href="https://alexandra.dk/about-the-alexandra-institute/">Alexandra Institute</a>. 
If you have found a bug, have questions about the usage of the library, or you are missing functionality, feel free to contact us at <a href="fresco@alexandra.dk">fresco@alexandra.dk</a>. 
