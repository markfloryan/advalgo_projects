#include <cstdint>
#include <iostream>
#include <random>

#define RNG_SEED 12345
typedef uint64_t u64;

bool naive(u64 n) {
	if (n < 2) return false;

	// Check all numbers if they are factors of n
	for (u64 i = 2; i < n; ++i) {
		if (n % i == 0) {
			return false;
		}
	}

	// If no factors were found, n is prime
	return true;
}

bool naiveOptimized(u64 n) {
	// Simple cases
	if (n < 4) return n == 2 || n == 3;
	// Skip all even numbers
	if ((n & 1) == 0) return false;

	// Check all numbers up to the square root of n
	for (u64 i = 3; i * i <= n; i += 2) {
		if (n % i == 0) {
			return false;
		}
	}

	// If no factors were found, n is prime
	return true;
}

// Helper function for fast binary exponentiation
u64 binary_power(u64 base, u64 exp, u64 mod) {
	u64 result = 1;
	base = base % mod;
	while (exp > 0) {
		if (exp & 1) {	// If exp is odd
			result = (result * base) % mod;
		}
		exp = exp >> 1;				 // Divide exp by 2
		base = (base * base) % mod;	 // Square the base
	}
	return result;
}

bool fermatPrime(u64 n, int iters) {
	// Simple cases
	if (n < 4) return n == 2 || n == 3;

	// using Mersenne Twister, but any random number generator will do
	// seed the generator with a constant value for reproducibility
	const unsigned seed = RNG_SEED;
	std::mt19937 gen(seed);
	std::uniform_int_distribution<u64> dist(2, n - 2);

	for (int i = 0; i < iters; ++i) {
		// Generate a random number in the range [2, n-2]
		u64 a = dist(gen);

		// Check if a^(n-1) is equiv to 1 mod n
		if (binary_power(a, n - 1, n) != 1) {
			return false;  // n is composite
		}
	}
	return true;
}

bool checkComposite(u64 n, u64 a, u64 d, int s) {
	u64 x = binary_power(a, d, n);
	if (x == 1 || x == n - 1) return false;

	// Eval case r == 0
	if (x == 1 or x == n - 1) {
		return false;
	}

	// Check the other cases for r
	for (int j = 1; j < s; ++j) {
		x = binary_power(x, 2, n);
		// Checking a^(2^r * d), accumulate x
		if (x == n - 1) return false;
	}

	return true;
}

bool millerRabin(u64 n, int iters) {
	if (n < 4) return n == 2 || n == 3;

	// Want to write n-1 as d*2^s, find suitable d and s
	// n is odd so d must be even (excluding n = 2)
	u64 s = 0;
	u64 d = n - 1;
	while ((d & 1) == 0) {
		d = d >> 1;	 // Divide d by 2
		s++;		 // Increment s
	}

	// using Mersenne Twister, but any random number generator will do
	// seed the generator with a constant value for reproducibility
	const unsigned seed = RNG_SEED;
	std::mt19937 gen(seed);
	std::uniform_int_distribution<u64> dist(2, n - 2);

	for (int i = 0; i < iters; ++i) {
		// Generate a random number in the range [2, n-2]
		u64 a = dist(gen);
		if (checkComposite(n, a, d, s)) {
			return false;
		}
	}
	return true;  // n is *probably* prime
}

bool millerRabinDeterministic(u64 n, int iters) {
	if (n < 4) return n == 2 || n == 3;

	// Want to write n-1 as d*2^s, find suitable d and s
	// n is odd so d must be even (excluding n = 2)
	u64 s = 0;
	u64 d = n - 1;
	while ((d & 1) == 0) {
		d = d >> 1;	 // Divide d by 2
		s++;		 // Increment s
	}

	// Only need to check first 12 priems for 64 bit ints
	u64 primes[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
	for (int i = 0; i < iters; ++i) {
		// Is a prime
		if (primes[i] == n) return true;

		if (checkComposite(n, primes[i], d, s)) {
			return false;
		}
	}
	return true;  // n is *probably* prime
}

int main(int argc, char* argv[]) {
	std::cout << "Hello, World!" << std::endl;

	return 0;
}