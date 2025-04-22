import sys
import os
import time

matching_pairs = {
    'A': 'T',
    'T': 'A',
    'C': 'G',
    'G': 'C',
    '#': '#',
}
#main idea: Normally, manacher's algo can guarantee that the left half of a palindrome is the same as the right half of a palindrome (after the palindrome is checked at a given center). The values on the right half of the center are also at least as much as either the min(right bound (current value to the right bound) or the left half's corresponding value). However, an issue arises with the center value when performing a matching palindrome instead of a normal one. Manacher's algo can guarantee that the left side of the array matches with the right side at a given center. However, the center value of the current is not fully checked in this scenario and some issues may arise that normally don't happen.

# The center may be a part of some other palindrome from the left side, and it may be a match with the corresponding values from left half's palindromes (with respect to each left half's palindromes center) , however, it MUST match with the corresponding values in right hand sided palindromes in order to use the saved results from the left side, and this may not always happen. So, at an index i, and a center c, you must check if the value at 2i - c(corresponding letter in palindrome with index in the center of the palindrome) matches with the value at center c. If it does, you may fully use the saved value, but if it doesn't and if the saved value exceeds 2i-c, you can only use 2i - c- 1 of the saved value as every value before the corresponding value of the center is guaranteed to match, but the center's corresponding value does not. 

def preprocess(s): #adds a # to the beginning and end of the string and adds a # between each character in the string
    s = "#" + "#".join(s) + "#" 
    return s
def postprocess(s): #removes all the # from the string
    s = s.replace("#", "")
    return s
def matches(a, b): # Check if two characters are matching pairs
    if matching_pairs[a] == b:
        return True
    return False


def helix(s):
    s = preprocess(s)
    
    radius = [0] * len(s) 
    center, right = 0, 0

    for i in range(1, len(s) - 1):
        #calculate mirrored position of i with respect to center
        mirrored_pos = 2 * center - i 
        
        #checks the value of the mirrored position, we can only use the value if its less than the right edge of the palindrome, otherwise we can only determine that the radius is atmost the distance to the right edge of the palindrome
        if i < right:
            #calculate center position with respect to i,
            distance_from_center = i - center
            corresponding_center_mirror = i + distance_from_center #finds the corresponding mirroed position of the center with respect to i
            # Check if the corresponding center mirror is within bounds and matches the character at the center
            if corresponding_center_mirror < len(s) and matches(s[corresponding_center_mirror], s[center]):
                radius[i] = min(right - i, radius[mirrored_pos]) 
            else: 
                # If the center character doesn't match, we can only use the distance up to the center -1
                radius[i] = min(right - i, distance_from_center - 1, radius[mirrored_pos])

        #expand the palindrome out until it either hits the edge of the string or a mismatch is found
        while i + radius[i] + 1 < len(s) and i - (radius[i] + 1) >= 0 and matches(s[i + radius[i] + 1], s[i - (radius[i] + 1)]):
            radius[i] += 1
        #update center0 and right 0
        if i + radius[i] > right:
            center = i
            right = i + radius[i]

    largest_radius = 0
    largest_center = 0
    #choose string with largest radius
    for i in range(0, len(s)):
        if radius[i] > largest_radius:
            largest_radius = radius[i]
            largest_center = i
    # print(radius)
    # print(s)
    return postprocess(s[largest_center - largest_radius:largest_center + largest_radius + 1])


# start_time = time.perf_counter()
input = sys.stdin.readline().strip()
output = helix(input)
# end_time = time.perf_counter()
# time_spent = end_time - start_time
# print(f"Time spent: {time_spent:.6f} seconds")

print(output)
