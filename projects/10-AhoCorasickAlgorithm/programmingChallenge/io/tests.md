# Test Case Explanations

### Basic Aho-Corasick Algorithm
These tests measure whether the basic Aho-Corasick algorithm without dynamic additions is implemented directly. Each test is designed to test one additional constraint / edge case, allowing for easier debugging.

1.
- Books contain at most one keyword
- Keywords are complete words
2.
- Books may contain multiple keywords
- Keywords are complete words
- Keywords have suffix overlap
3.
- Books may contain multiple keywords
- Keywords may be sub-words
- Keywords have suffix overlap
4.
- Books may contain multiple keywords
- Keywords may be sub-words
- Keywords have suffix overlap
- Keywords may be substrings of other keywords
5.
- Books may contain multiple keywords
- Keywords may be sub-words
- Keywords have suffix overlap
- Keywords may be substrings of other keywords
- Multiple keywords can match simultaneously
6.
- Books may contain multiple keywords
- Keywords may be sub-words
- Must handle case insensitivity
- Keywords have suffix overlap
- Keywords may be substrings of other keywords
- Multiple keywords can match simultaneously

### Dynamic Additions
The following test cases test support for dynamic additions to the dictionary set. These test cases must meet all the requirements of the Basic Aho-Corasick test cases listed above.

7.
- Additions will not affect other suffix links
8.
- Additions may affect other suffix links
9.
- Additions may affect other suffix links
- Additions may affect suffix links that have already been updated
10.
- Additions may affect other suffix links
- Additions may affect suffix links that have already been updated
- Avoids double-counting duplicate keywords
11.
- Tests support for the maximum keyword and book size, plus all available special characters

### Speed Cases
The following cases are designed test for time complexity issues. While the other test cases may enforce time limits, these cases validate the algorithm's performance under specific load types. Unless otherwise stated, the test cases uses the following default values:

- B (number of books): 1,000
- K (number of keywords): 50
- A (number of insertions): 10
- S (number of searches): 5

12.
13. B = 20,000
14. K = 1,000
15. A = 300; S = 100
16. A = 750; S = 250
17. B = 20,000; K = 1,000
18. B = 20,000; A = 300; S = 100
19. B = 20,000; K = 1,000; A = 300; S = 100
20. B = 20,000; K = 1,000; A = 750; S = 250