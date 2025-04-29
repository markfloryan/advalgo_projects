# Explanation Of Test Cases

These test cases are designed to test various different input types, sizes, and potential edge cases. An explanation of each test case is as follows:

Cases 1-6 are handcrafted, and cases 7-20 are computer generated with varying parameters.

 ## Handcrafted Tests
 - 1: small nontrivial test case meant as a sample
  - 2: tests multiple customers, negative coin labels, and inputs which require output labels with absolute value greater than 10000
 - 3: tests lots of input labels that are close together with multiple customers
 - 4: tests a large value of k meant to overflow if you read it as an int
 - 5: tests for absence of weird behavior in the trivial case (k=1, n=1, only one coin)
 - 6: tests both a large value of k and input labels far away from each other

 ## Computer Generated Tests

These tests are randomly generated, but with different values of k (the number to multiply the values of the piles by), n (the number of customers), and the distribution of each customer's coins. The generation parameters are as follows:

 - 7: k ~ 10^500,  n = 1,  piles ~65% dense in [-9000, -8000] U [8000, 9000]
 - 8: k ~ 10^500,  n = 2,  piles ~3% dense in [-1000, 1000]
 - 9: k ~ 10^750,  n = 1,  piles ~10% dense in [-100, 100]
- 10: k ~ 10^900,  n = 10, piles ~70% dense in [-1e4, 1e4]
- 11: k = (1250 nines), n = 9,  piles ~50% dense in [-1e4, 1e4]
- 12: k ~ 10^1753, n = 10, piles ~50% dense in [-1e4, 1e4]
- 13: k ~ 10^2500, n = 10, piles ~50% dense in [-1e4, 1e4]
- 14: k ~ 10^3000, n = 10, piles ~98% dense in [-1e4, 1e4]
- 15: k ~ 10^3250, n = 10, piles ~50% dense in [3e3, 8e3]
- 16: k ~ 10^3500, n = 10, piles ~60% dense in [-3e3, -1e3]
- 17: k ~ 10^3500, n = 10, piles ~95% dense in [-1e3, 1e3]
- 18: k ~ 10^3988, n = 10, exactly 20 numbers in [-1e4, 1e4]
- 19: k = (3989 sevens with a few mutations), n = 10, piles ~45% dense in [-5e3, 1e4]
- 20: k ~ 10^4000, n = 10, piles ~60% dense in [-1e4, 1e4]
