Project 18 - Fast Multiplication
===============================

Go [back to home page](../../index.html)

<a name="overview"></a>Executive Summary
---------------------------------------

- [Executive Summary](./executiveSummary.pdf)

<a name="overview"></a>Implementation
---------------------------------------

- Implementations:
	- [C++](./implementation/fast_multiplication.cc)
	- [Java](./implementation/FastMultiplication.java)
	- [Python](./implementation/fast_multiplication.py)
	- io
		- [test input 1](./implementation/io/sample.in.1)
		- [test output 1](./implementation/io/sample.out.1)
		- [test input 2](./implementation/io/sample.in.2)
		- [test output 2](./implementation/io/sample.out.2)
        - [test input 3](./implementation/io/sample.in.3)
		- [test output 3](./implementation/io/sample.out.3)

<a name="overview"></a>Slides
---------------------------------------

- [Instructional Slides](./slides/lecture_fastMultiplication.pptx)
- [Presentation Slides](./slides/presentation_fastMultiplication.pptx)


<a name="overview"></a>Programming Challenge
---------------------------------------

- [Programming Challenge](./programmingChallenge/problemStatement.pdf)
	- Solutions:
		- [C++](./programmingChallenge/solutions/pcSol_cpp.cpp)
		- [Java](./programmingChallenge/solutions/pcSol_java.java)
		- [Python](./programmingChallenge/solutions/pcSol_python.py)
	- Test Cases:
		- Case 1: programming challenge example
		- [Case 1 input](./programmingChallenge/io/test.in.1)
		- [Case 1 output](./programmingChallenge/io/test.out.1)
		- Case 2: edge case - every ship along y = x
        - [Case 2 input](./programmingChallenge/io/test.in.2)
		- [Case 2 output](./programmingChallenge/io/test.out.2)
		- Case 3: edge case - every ship along fixed radius
        - [Case 3 input](./programmingChallenge/io/test.in.3)
		- [Case 3 output](./programmingChallenge/io/test.out.3)
		- Case 4: edge case - duplicate ship coordinates
        - [Case 4 input](./programmingChallenge/io/test.in.4)
		- [Case 4 output](./programmingChallenge/io/test.out.4)
		- Case 5: edge case - no ships in query
        - [Case 5 input](./programmingChallenge/io/test.in.5)
		- [Case 5 output](./programmingChallenge/io/test.out.5)
		- Case 6: edge case - 20 ships provided in sorted polar order
        - [Case 6 input](./programmingChallenge/io/test.in.6)
		- [Case 6 output](./programmingChallenge/io/test.out.6)
		- Case 7: edge case - 20 ships provided in reverse polar order
        - [Case 7 input](./programmingChallenge/io/test.in.7)
		- [Case 7 output](./programmingChallenge/io/test.out.7)
		- Case 8: edge case - 20 ships along x = 0
        - [Case 8 input](./programmingChallenge/io/test.in.8)
		- [Case 8 output](./programmingChallenge/io/test.out.8)
		- Case 9: edge case - 20 ships along y = 0
        - [Case 9 input](./programmingChallenge/io/test.in.9)
		- [Case 9 output](./programmingChallenge/io/test.out.9)
		- Case 10: runtime: 1,000 random ships and 10 queries
        - [Case 10 input](./programmingChallenge/io/test.in.10)
		- [Case 10 output](./programmingChallenge/io/test.out.10)
		- Case 11: runtime: 5,000 random ships and 10 queries
        - [Case 11 input](./programmingChallenge/io/test.in.11)
		- [Case 11 output](./programmingChallenge/io/test.out.11)
		- Case 12: runtime: 10,000 random ships and 10 queries
        - [Case 12 input](./programmingChallenge/io/test.in.12)
		- [Case 12 output](./programmingChallenge/io/test.out.12)
		- Case 13: runtime: 50,000 random ships and 10 queries
        - [Case 13 input](./programmingChallenge/io/test.in.13)
		- [Case 13 output](./programmingChallenge/io/test.out.13)
		- Case 14: runtime: 150,000 sorted ships and 10 queries
        - [Case 14 input](./programmingChallenge/io/test.in.14)
		- [Case 14 output](./programmingChallenge/io/test.out.14)
		- Case 15: runtime: 150,000 reverse sorted ships and 10 queries
        - [Case 15 input](./programmingChallenge/io/test.in.15)
		- [Case 15 output](./programmingChallenge/io/test.out.15)
		- Case 16: runtime: 150,000 random ships and 10 queries
        - [Case 16 input](./programmingChallenge/io/test.in.16)
		- [Case 16 output](./programmingChallenge/io/test.out.16)
		- Case 17: runtime: 150,000 sorted ships with 150,000 reverse sorted ships and 10 queries
        - [Case 17 input](./programmingChallenge/io/test.in.17)
		- [Case 17 output](./programmingChallenge/io/test.out.17)
		- Case 18: runtime: 300,000 sorted ships and 10 queries
        - [Case 18 input](./programmingChallenge/io/test.in.18)
		- [Case 18 output](./programmingChallenge/io/test.out.18)
		- Case 19: runtime: 300,000 reverse sorted ships and 10 queries
        - [Case 19 input](./programmingChallenge/io/test.in.19)
		- [Case 19 output](./programmingChallenge/io/test.out.19)
		- Case 20: runtime: 300,000 random ships and 10 queries
        - [Case 20 input](./programmingChallenge/io/test.in.20)
		- [Case 20 output](./programmingChallenge/io/test.out.20)
