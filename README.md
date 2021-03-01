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

There is no assumption on how the data is distributed among the parties -- the data may 
be divided horizontally, where each party has a different set of data entries, or vertically, 
where each party has different attributes for the same entries, or the data could be intermediate 
results from other secure multi-party computations.

The library has the following functionality:
1. Statistics
    * Descriptive statistics
        * Sample mean
        * Sample standard deviation
        * Sample variance
        * Simple and multi-dimensional histogram
        * Frequency table
        * Sample correlation
    * Regression
        * Simple linear regression
        * Multivariate linear regression
        * Logistic regression via gradient descent 
    * Statistical tests
        * &chi;<sup>2</sup>-test
        * F-test
        * One- and two-sample t-test
        * Kruskall-Wallis test
    * Survival analysis
        * Cox regression
    * Misc.
        * <i>k</i>-anonymization
1. Sampling from the following distributions
    * Bernoulli distribution
    * Categorical distribution
    * Exponential distribution
    * Irwin-Hall distribution
    * Laplace distribution
    * Normal distribution
    * Rademacher distribution
    * Uniform distribution
1. Advanced linear algebra (see also the <i>fixed</i> library in FRESCO for basic linear algebra functions)
    * Back- and forward substitution
    * Gram-Schmidt process
    * Inverse of triangular matrices
    * Moore-Penrose pseudo inverse
    * QR-algorithm for eigenvalue computation
    * QR-decomposition


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
1. [Sampler](core/src/main/java/dk.alexandra.fresco.stat/Sampler.java)  Draw samples from various distributions.
1. [Linear Algebra](core/src/main/java/dk.alexandra.fresco.stat/LinearAlgebra.java) Advanced linear algebra.

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
in the root directory of the demo-module. To run a demo, execute eg.
```
java -jar target/survival-jar-with-dependencies.jar 1
java -jar target/survival-jar-with-dependencies.jar 2
```
in two separate terminals and change the name of the jar to the desired demo.

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