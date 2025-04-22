import subprocess
import random
import string

def generate_random_string(length):
    return ''.join(random.choices(string.ascii_lowercase, k=length))

def run_program(command, input_str):
    result = subprocess.run(command, input=input_str.encode(), capture_output=True)
    return result.stdout.decode().strip()

def main():
    cpp_exec = "./burrowWheeler"        # compiled C++ binary
    py_exec = ["python3", "burrowsWheeler.py"]  # Python script

    for i in range(100):
        s = generate_random_string(random.randint(5, 20))

        cpp_out = run_program([cpp_exec], s)
        py_out = run_program(py_exec, s)

        if cpp_out != py_out:
            print(f"❌ Mismatch on input: {s}")
            print(f" C++ output:\n{cpp_out}")
            print(f" Python output:\n{py_out}")
            return

    print("✅ All tests passed.")

if __name__ == "__main__":
    main()
