#include <iostream>
#include <random>
// see primalityTests.cpp under implementation for a more detailed explanation

#define u128 __uint128_t
#define K 15

std::mt19937 engine(std::random_device{}());

// Use binary exponentiation to compute a^b mod m
u128 binPow(u128 a, u128 b, u128 m) {
	u128 r = 1;
	a %= m;	 // If a >= m, we can reduce it

	while (b > 0) {
		// If b is odd, multiply a with result
		if (b & 1) {
			r = (r * a) % m;
		}

		b >>= 1;
		a = (a * a) % m;
	}
	return r;
}

bool checkComposite(u128 n, u128 a, u128 s, u128 d) {
	u128 r = binPow(a, d, n);
	// probably prime
	if (r == 1 || r == n - 1) return false;

	// Check the factors
	for (u128 i = 1; i < s; ++i) {
		r = binPow(r, 2, n);
		if (r == n - 1) return false;
	}

	return true;
}

bool isPrime(u128 n) {
	if (n < 4) return n == 2 || n == 3;

	// Compute 2^s * d = n - 1
	u128 a, s = 0;
	u128 d = n - 1;
	while ((d & 1) == 0) {
		s++;
		d >>= 1;
	}

	std::uniform_int_distribution<u128> dist(2, n - 2);

	// Witness loop
	for (int i = 0; i < K; ++i) {
		a = dist(engine);
		if (checkComposite(n, a, s, d)) {
			return false;
		}
	}

	return true;
}

// Hash function for fartcoin
u128 fartcoinHash(std::string &s) {
	u128 h = 0;
	for (char c : s) {
		h <<= 1;
		h ^= c;
	}
	return h;
}

int main(int argc, char *argv[]) {
	int n;
	std::cin >> n;
	int chainLength = 0;
	std::string chain = "fartcoin";

	std::string name, nonce, temp;
	u128 h;

	for (int i = 0; i < n; ++i) {
		std::cin >> name >> nonce;

		temp = chain + nonce;
		h = fartcoinHash(temp);

		if (isPrime(h)) {
			// Handle adding to chain
			chainLength++;
			chain = temp;
			std::cout << name << " accepted " << chainLength << std::endl;
		} else {
			std::cout << name << " rejected " << chainLength << std::endl;
		}
	}
	return 0;
}