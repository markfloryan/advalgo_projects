rm --force implementation.exe implementation;
g++ -O2 -o implementation implementation.cpp;
cat io/sample.in | ./implementation > io/sample.out;