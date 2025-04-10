// Implementation of Toom-3 algorithm for fast multiplication

use std::env;

fn calculate_base(a: i128, b: i128) -> i128 {
    // The base used in multiplication is a power of some number called radix
    // In a computer system, this is usually 2, but we can use 10 for human-friendliness
    let radix: f64 = 10.0;
    // Calculate the number of digits in the operands
    let num_digits_a = (a.abs() as f64).log(radix).ceil();
    let num_digits_b = (b.abs() as f64).log(radix).ceil();
    // Take the maximum number of digits in the operands and divide by 3
    // Take the ceiling in case the number of digits is not divisible by 3
    let num_digits_max = num_digits_a.max(num_digits_b);
    let power = (num_digits_max / 3.0).ceil() as u32;
    // Calculate the base as radix^power
    (radix as i128).pow(power)
}

fn split(number: i128, base: i128) -> [i128; 3] {
    // Take the lowest third of the number and store it in parts[0]
    let part0 = number % base;
    // Remove the lowest third of the number
    let number = number / base;
    // Take the next third of the number and store it in parts[1]
    let part1 = number % base;
    // Remove the next third of the number
    let number = number / base;
    // Take the last third of the number and store it in parts[2]
    let part2 = number;
    [part0, part1, part2]
}

fn combine(parts: [i128; 5], base: i128) -> i128 {
    // Combine the parts into a single number
    // The number at parts[i] is the coefficient of base^i
    // We calculate coefficient times base^i and add them together
    // The result is the final product
    let mut result: i128 = 0;
    for (i, &part) in parts.iter().enumerate() {
        // Calculate parts[i] * base^i
        let num: i128 = part * base.pow(i as u32);
        result += num;
    }
    result
}

fn multiply(a: i128, b: i128) -> i128 {
    // Recursive base case: if either operand is 0, return 0
    if a == 0 || b == 0 {
        return 0;
    }
    // Recursive base case: one operand is in [-9, 9] -- return a * b
    if a.abs() < 10 || b.abs() < 10 {
        return a * b;
    }

    // Calculate the base for splitting the operands
    // Every part of the operand will be less than the base
    let base = calculate_base(a, b);

    // We can split the operand into 3 roughly equal-sized parts
    // The operand a is equivalent to the polynomial A(x) = a_0 + a_1 x + a_2 x^2, where x = base
    // Therefore, aParts[0] = a_0, aParts[1] = a_1, aParts[2] = a_2
    let a_parts = split(a, base);
    // The operand b is equivalent to the polynomial B(x) = b_0 + b_1 x + b_2 x^2, where x = base
    // Therefore, bParts[0] = b_0, bParts[1] = b_1, bParts[2] = b_2
    let b_parts = split(b, base);

    // We can represent the product as a polynomial C(x) = c_0 + c_1 x + c_2 x^2 + c_3 x^3 + c_4 x^4, where x = base
    // Therefore, products[0] = c_0, products[1] = c_1, products[2] = c_2, products[3] = c_3, products[4] = c_4
    let mut products = [0i128; 5];

    // Let's do some polynomial multiplication!
    // We know that c_0 + c_1 x + c_2 x^2 + c_3 x^3 + c_4 x^4 = (a_0 + a_1 x + a_2 x^2) * (b_0 + b_1 x + b_2 x^2)
    // We also know that (a_0 + a_1 x + a_2 x^2) * (b_0 + b_1 x + b_2 x^2) = (a_0 b_0) + (a_1 b_0 + a_0 b_1) x + (a_2 b_0 + a_1 b_1 + a_0 b_2) x^2 + (a_2 b_1 + a_1 b_2) x^3 + (a_2 b_2) x^4
    // Multiplying these parts out is no faster than standard multiplication, though...

    // Instead, we'll evaluate the polynomial at 5 points: x = 0, 1, -1, 2, and infinity
    let x0 = multiply(a_parts[0], b_parts[0]); // C(0) = c_0 + c_1 * 0 + c_2 * 0^2 + c_3 * 0^3 + c_4 * 0^4 = c_0 = a_0 * b_0
    let x1 = multiply(
        a_parts[0] + a_parts[1] + a_parts[2],
        b_parts[0] + b_parts[1] + b_parts[2],
    ); // C(1) = c_0 + c_1 * 1 + c_2 * 1^2 + c_3 * 1^3 + c_4 * 1^4 = c_0 + c_1 + c_2 + c_3 + c_4 = (a_0 + a_1 + a_2) * (b_0 + b_1 + b_2)
    let x2 = multiply(
        a_parts[0] - a_parts[1] + a_parts[2],
        b_parts[0] - b_parts[1] + b_parts[2],
    ); // C(-1) = c_0 + c_1 * -1 + c_2 * (-1)^2 + c_3 * (-1)^3 + c_4 * (-1)^4 = c_0 - c_1 + c_2 - c_3 + c_4 = (a_0 - a_1 + a_2) * (b_0 - b_1 + b_2)
    let x3 = multiply(
        a_parts[0] + 2 * a_parts[1] + 4 * a_parts[2],
        b_parts[0] + 2 * b_parts[1] + 4 * b_parts[2],
    ); // C(2) = c_0 + c_1 * 2 + c_2 * 2^2 + c_3 * 2^3 + c_4 * 2^4 = c_0 + 2c_1 + 4c_2 + 8c_3 + 16c_4 = (a_0 + 2a_1 + 4a_2) * (b_0 + 2b_1 + 4b_2)
    let x4 = multiply(a_parts[2], b_parts[2]); // C(infinity) = c_4 = a_2 * b_2 -- because the other terms are negligible in comparison to the highest degree term

    // Now let's fill in the coefficients of the product polynomial
    products[0] = x0; // c_0 = C(0)
    products[4] = x4; // c_4 = C(infinity)
    // C(1) + C(-1) = (c_0 + c_1 + c_2 + c_3 + c_4) + (c_0 - c_1 + c_2 - c_3 + c_4) = 2c_0 + 2c_2 + 2c_4
    // Therefore, c_2 = (C(1) + C(-1)) / 2 - c_0 - c_4
    products[2] = (x1 + x2) / 2 - products[0] - products[4];
    // C(2) - 2C(1) = (c_0 + 2c_1 + 4c_2 + 8c_3 + 16c_4) - 2(c_0 + c_1 + c_2 + c_3 + c_4) = -c_0 + 2c_2 + 6c_3 + 14c_4
    // Therefore, c_3 = (C(2) - 2C(1) + c_0 - 2c_2 - 14c_4) / 6
    products[3] = (x3 - 2 * x1 + products[0] - 2 * products[2] - 14 * products[4]) / 6;
    // c_1 = C(1) - c_0 - c_2 - c_3 - c_4
    products[1] = x1 - products[0] - products[2] - products[3] - products[4];

    // Now that we have the coefficients of C(x), we just solve for C(base)
    // C(base) = c_0 + c_1 * base + c_2 * base^2 + c_3 * base^3 + c_4 * base^4 = our result
    combine(products, base)
}

fn main() {
    // Get operands from command line arguments
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 {
        eprintln!("Usage: {} <operandA> <operandB>", args[0]);
        return;
    }
    // Multiply
    let operand_a: i128 = args[1].parse().expect("Invalid integer for operand A");
    let operand_b: i128 = args[2].parse().expect("Invalid integer for operand B");
    // Compute the product using Toom‑3 multiplication.
    let result = multiply(operand_a, operand_b);
    println!("{}", result);
}
