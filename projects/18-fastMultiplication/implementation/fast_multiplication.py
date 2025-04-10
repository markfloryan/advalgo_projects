# Implementation of Toom-3 algorithm for fast multiplication

from math import ceil, log
import sys

sys.setrecursionlimit(50000)

def calculateBase(a: int, b: int) -> int:
    radix = 10
    numDigitsA = ceil(log(abs(a), radix))
    numDigitsB = ceil(log(abs(b), radix))
    numDigitsMax = max(numDigitsA, numDigitsB)
    power = ceil(numDigitsMax / 3)
    return int(radix ** power)

def split(number: int, base: int) -> list[int]:
    parts = [0, 0, 0]
    parts[0] = number % base
    number //= base
    parts[1] = number % base
    number //= base
    parts[2] = number
    return parts

def combine(parts: list[int], base: int) -> int:
    result = 0
    for i in range(len(parts)):
        num = parts[i] * (base ** i)
        result += num
    return result

def multiply(a: int, b: int) -> int:
    if a == 0 or b == 0:
        return 0
    if abs(a) <= 10 or abs(b) <= 10:
        return a * b
    base = calculateBase(a, b)
    a_parts = split(a, base)
    b_parts = split(b, base)
    products = [0, 0, 0, 0, 0]
    x0 = multiply(a_parts[0], b_parts[0])
    x1 = multiply((a_parts[0] + a_parts[1] + a_parts[2]), (b_parts[0] + b_parts[1] + b_parts[2]))
    x2 = multiply((a_parts[0] - a_parts[1] + a_parts[2]), (b_parts[0] - b_parts[1] + b_parts[2]))
    x3 = multiply((a_parts[0] + 2 * a_parts[1] + 4 * a_parts[2]), (b_parts[0] + 2 * b_parts[1] + 4 * b_parts[2]))
    x4 = multiply(a_parts[2], b_parts[2])
    products[0] = x0
    products[4] = x4
    products[2] = (x1 + x2) // 2 - products[0] - products[4]
    products[3] = (x3 - 2 * x1 + products[0] - 2 * products[2] - 14 * products[4]) // 6
    products[1] = x1 - products[0] - products[2] - products[3] - products[4]
    return combine(products, base)

if __name__ == "__main__":
    if len(sys.argv) >= 3:
        operand_a: int = int(sys.argv[1])
        operand_b: int = int(sys.argv[2])
        result: int = multiply(operand_a, operand_b)
        print(result)
    else:
        print("Running basic Toom-Cook multiply tests...")
        for a in range(-1000, 1000, 7):
            for b in range(-10, 1000, 11):
                print(f"{a=}, {b=}")
                expected = a * b
                actual = multiply(a, b)
                assert actual == expected, f"Test failed: {a} * {b} = {actual}, expected {expected}"
        print("All tests passed.")
