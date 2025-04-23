#include <iostream>
#include <random>
#include <bits/stdc++.h>
using namespace std;

int keywordSize = 256;
int titleSize = 512;

void printKeyword()
{
    for (int i = rand() % keywordSize; i >= 0; i--)
        cout << (char)((rand() % 26) + 'a');

    cout << "\n";
}

void printBookTitle(vector<char> &chars) {
    for (int j = rand() % titleSize; j >= 0; j--)
    {
        cout << chars[rand() % chars.size()];
    }
    cout << "\n";
}

int main()
{
    int B, K, A, S;
    cin >> B >> K >> A >> S >> titleSize >> keywordSize;

    srand((unsigned int)time(NULL));

    vector<char> bookChars;
    for (char i = 'a'; i <= 'z'; i++)
        bookChars.emplace_back(i);

    for (char i = 'A'; i <= 'Z'; i++)
        bookChars.emplace_back(i);

    for (char i = '0'; i <= '9'; i++)
        bookChars.emplace_back(i);

    bookChars.emplace_back(' ');
    bookChars.emplace_back(',');
    bookChars.emplace_back('.');
    bookChars.emplace_back('?');
    bookChars.emplace_back('!');
    bookChars.emplace_back(':');
    bookChars.emplace_back(';');
    bookChars.emplace_back('-');

    cout << B << "\n";
    for (int i = 0; i < B; i++)
        printBookTitle(bookChars);

    cout << K << "\n";
    for (int i = 0; i < K; i++)
        printKeyword();

    char previous = 'A';
    cout << A + S << "\n";
    while (A + S > 1)
    {
        if (previous == 'S' && A > 0)
        {
            A--;
            previous = 'A';

            cout << "A ";
            printKeyword();
            continue;
        }

        if (rand() % 3 == 0 && S > 1)
        {
            S--;
            previous = 'S';

            cout << "S\n";
            continue;
        }

        else
        {
            A--;
            previous = 'A';

            cout << "A ";
            printKeyword();
            continue;
        }
    }

    cout << "S\n";
}