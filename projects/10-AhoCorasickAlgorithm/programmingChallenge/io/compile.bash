rm --force newTest.exe newTest;
g++ -O2 -o newTest generateTest.cpp;
cat params.in | ./newTest > test.in;