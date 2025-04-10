# Implementation of Toom-3 algorithm for fast multiplication

from math import ceil, log
import sys

def calculateBase(a: int, b: int) -> int:
    # The base used in multiplication is a power of some number called radix
    # In a computer system, this is usually 2, but we can use 10 for human-friendliness
    radix = 10
    # Calculate the number of digits in the operands
    numDigitsA = ceil(log(a, radix))
    numDigitsB = ceil(log(b, radix))
    # Take the maximum number of digits in the operands and divide by 3
    # Take the ceiling in case the number of digits is not divisible by 3
    numDigitsMax = max(numDigitsA, numDigitsB)
    power = ceil(numDigitsMax / 3)
    # Calculate the base as radix^power
    return int(radix ** power)

def split(number: int, base: int) -> list[int]:
    # Split the number into 3 parts
    parts = [0, 0, 0]
    # Take the lowest third of the number and store it in parts[0]
    parts[0] = number % base
    # Remove the lowest third of the number
    number //= base
    # Take the next third of the number and store it in parts[1]
    parts[1] = number % base
    # Remove the next third of the number
    number //= base
    # Take the last third of the number and store it in parts[2]
    parts[2] = number
    return parts

def combine(parts: list[int], base: int) -> int:
    # Combine the parts into a single number
    # The number at parts[i] is the coefficient of base^i
    # We calculate coefficient times base^i and add them together
    # The result is the final product
    result = 0
    for i in range(len(parts)):
        # Calculate parts[i] * base^i
        num = parts[i] * (base ** i)
        result += num
    return result

def multiply(a: int, b: int) -> int:
    # Calculate the base for splitting the operands
    # Every part of the operand will be less than the base
    base = calculateBase(a, b)
    # If the operands are small enough, we can use the standard multiplication
    # This is the base case for the recursion
    if a < base and b < base:
        return a * b

    # We can split the operand into 3 roughly equal-sized parts
    # The operand a is equivalent to the polynomial A(x) = a_0 + a_1 x + a_2 x^2, where x = base
    # Therefore, a_parts[0] = a_0, a_parts[1] = a_1, a_parts[2] = a_2
    a_parts = split(a, base)
    # The operand b is equivalent to the polynomial B(x) = b_0 + b_1 x + b_2 x^2, where x = base
    # Therefore, b_parts[0] = b_0, b_parts[1] = b_1, b_parts[2] = b_2
    b_parts = split(b, base)

    # We can represent the product as a polynomial C(x) = c_0 + c_1 x + c_2 x^2 + c_3 x^3 + c_4 x^4, where x = base
    # Therefore, products[0] = c_0, products[1] = c_1, products[2] = c_2, products[3] = c_3, products[4] = c_4
    products = [0, 0, 0, 0, 0]

    # Let's do some polynomial multiplication!
    # We know that c_0 + c_1 x + c_2 x^2 + c_3 x^3 + c_4 x^4 = (a_0 + a_1 x + a_2 x^2) * (b_0 + b_1 x + b_2 x^2)
    # We also know that (a_0 + a_1 x + a_2 x^2) * (b_0 + b_1 x + b_2 x^2) = (a_0 b_0) + (a_1 b_0 + a_0 b_1) x + (a_2 b_0 + a_1 b_1 + a_0 b_2) x^2 + (a_2 b_1 + a_1 b_2) x^3 + (a_2 b_2) x^4
    # Multiplying these parts out is no faster than standard multiplication, though...

    # TODO: recursive calls
    # Instead, we'll evaluate the polynomial at 5 points: x = 0, 1, -1, 2, and infinity
    x0 = a_parts[0] * b_parts[0] # C(0) = c_0 + c_1 * 0 + c_2 * 0^2 + c_3 * 0^3 + c_4 * 0^4 = c_0 = a_0 * b_0
    x1 = (a_parts[0] + a_parts[1] + a_parts[2]) * (b_parts[0] + b_parts[1] + b_parts[2]) # C(1) = c_0 + c_1 * 1 + c_2 * 1^2 + c_3 * 1^3 + c_4 * 1^4 = c_0 + c_1 + c_2 + c_3 + c_4 = (a_0 + a_1 + a_2) * (b_0 + b_1 + b_2)
    x2 = (a_parts[0] - a_parts[1] + a_parts[2]) * (b_parts[0] - b_parts[1] + b_parts[2]) # C(-1) = c_0 + c_1 * -1 + c_2 * (-1)^2 + c_3 * (-1)^3 + c_4 * (-1)^4 = c_0 - c_1 + c_2 - c_3 + c_4 = (a_0 - a_1 + a_2) * (b_0 - b_1 + b_2)
    x3 = (a_parts[0] + 2 * a_parts[1] + 4 * a_parts[2]) * (b_parts[0] + 2 * b_parts[1] + 4 * b_parts[2]) # C(2) = c_0 + c_1 * 2 + c_2 * 2^2 + c_3 * 2^3 + c_4 * 2^4 = c_0 + 2c_1 + 4c_2 + 8c_3 + 16c_4 = (a_0 + 2a_1 + 4a_2) * (b_0 + 2b_1 + 4b_2)
    x4 = a_parts[2] * b_parts[2] # C(infinity) = c_4 = a_2 * b_2 -- because the other terms are negligible in comparison to the highest degree term

    # Now let's fill in the coefficients of the product polynomial
    products[0] = x0 # c_0 = C(0)
    products[4] = x4 # c_4 = C(infinity)
    # C(1) + C(-1) = (c_0 + c_1 + c_2 + c_3 + c_4) + (c_0 - c_1 + c_2 - c_3 + c_4) = 2c_0 + 2c_2 + 2c_4
    # Therefore, c_2 = (C(1) + C(-1)) / 2 - c_0 - c_4
    products[2] = (x1 + x2) // 2 - products[0] - products[4]
    # C(2) - 2C(1) = (c_0 + 2c_1 + 4c_2 + 8c_3 + 16c_4) - 2(c_0 + c_1 + c_2 + c_3 + c_4) = -c_0 + 2c_2 + 6c_3 + 14c_4
    # Therefore, c_3 = (C(2) - 2C(1) + c_0 - 2c_2 - 14c_4) / 6
    products[3] = (x3 - 2 * x1 + products[0] - 2 * products[2] - 14 * products[4]) // 6
    # c_1 = C(1) - c_0 - c_2 - c_3 - c_4
    products[1] = x1 - products[0] - products[2] - products[3] - products[4]

    # Now that we have the coefficients of C(x), we just solve for C(base)
    # C(base) = c_0 + c_1 * base + c_2 * base^2 + c_3 * base^3 + c_4 * base^4 = our result
    return combine(products, base)

if __name__ == "__main__":
    # Get operands from command line arguments
    operand_a: int = int(sys.argv[1])
    operand_b: int = int(sys.argv[2])
    # Multiply
    result: int = multiply(operand_a, operand_b)
    # Print the result
    print(result)