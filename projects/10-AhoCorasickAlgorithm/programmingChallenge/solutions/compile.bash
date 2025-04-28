rm --force solution.exe solution;
g++ -O2 -o solution solution.cpp;
cat ../io/test.in | ./solution > ../io/test.out;