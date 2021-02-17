<h1 align="center">FRESCO-stat</h1>

<p align="center">
  Library for secure numerical computations, statistics and linear algebra on data held by multiple parties without sharing the data.
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#usage">Usage</a></li>
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

The functions in the library is accessible through three collections of functions:

1. [Statistics](src/main/java/dk/alexandra/fresco/stat/Statistics.java) Descriptive statistics, statistical tests and regression.
2. [Linear Algebra](src/main/java/dk/alexandra/fresco/stat/LinearAlgebra.java) Advanced linear algebra.
3. [Sampler](src/main/java/dk/alexandra/fresco/stat/Sampler.java)  Draw samples from various distributions.

They are used similarly to builders in FRESCO, namely by calling e.g.

```
Statistics.using(builder).chiSquare(observed, expected);
``` 

to perform a ꭓ²-test.

Note that overflows may happen during computation, which will likely appear as very large outputs. 
This may be avoided by normalizing the input data and/or using a bigger modulus in FRESCO.
 
<!-- CONTRIBUTING -->
## Contributing

If you want to help out developing new features for FRESCO-stat or fix a bug you've stumbled upon, it may be done as follows:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/MyFeature`)
3. Commit your Changes (`git commit -m 'Add MyFeature'`)
4. Push to the Branch (`git push origin feature/MyFeature`)
5. Open a Pull Request

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.