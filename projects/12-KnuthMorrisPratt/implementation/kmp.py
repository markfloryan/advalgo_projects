def constructLps(pat, lps):
    lps[0] = 0 # first element is always 0 (since prefix must be proper)

    for i in range(1, len(pat)):
        j = lps[i-1] # reset j based on previous lps value
        while (j > 0 and pat[i] != pat[j]): # fall back to shorter prefix until a match is found or j == 0
            j = lps[j-1]
        if (pat[i] == pat[j]): # if characters match, increase the length of the current lps
            j += 1
        lps[i] = j

def search(pat, txt):
    n = len(txt)
    m = len(pat)

    lps = [0] * m
    res = []

    constructLps(pat, lps)

    # Pointers i and j, for traversing 
    # the text and pattern
    i = 0
    j = 0

    while i < n:
        
        # If characters match, move both pointers forward
        if txt[i] == pat[j]:
            i += 1
            j += 1

            # If the entire pattern is matched 
            # store the start index in result
            if j == m:
                res.append(i - j)
                
                # Use LPS of previous index to 
                # skip unnecessary comparisons
                j = lps[j - 1]
        
        # If there is a mismatch
        else:
            
            # Use lps value of previous index
            # to avoid redundant comparisons
            if j != 0:
                j = lps[j - 1]
            else:
                i += 1
    return res 

if __name__ == "__main__":
    txt = input()
    pat = input()

    res = search(pat, txt)
    for i in range(len(res)):
        print(res[i], end=" ")
    print()
