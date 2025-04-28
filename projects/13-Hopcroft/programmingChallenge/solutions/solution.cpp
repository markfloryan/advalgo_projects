#include <bits/stdc++.h>
using namespace std;
using ll = long long;

int main(){
    ios::sync_with_stdio(false);
    cin.tie(NULL);

    int n;
    cin >> n;
    // original DFA transitions: next[state][0] for 'a', [1] for 'b'
    vector<array<int,2>> nxt(n);
    for(int i = 0; i < 2*n; i++){
        int p, q; char c;
        cin >> p >> c >> q;
        nxt[p][c=='b'] = q;
    }
    int s1, s2;
    cin >> s1 >> s2;
    int f;
    cin >> f;
    vector<bool> is_final(n,false);
    for(int i = 0; i < f; i++){
        int q; cin >> q;
        is_final[q] = true;
    }

    // 1) BFS over pairs (p,q)
    unordered_map<ll,int> pair_id;
    pair_id.reserve(n*4);
    auto encode = [&](int p,int q){ return (ll)p * n + q; };
    queue<pair<int,int>> q;
    q.push({s1,s2});
    pair_id[encode(s1,s2)] = 0;
    vector<pair<int,int>> states; 
    states.reserve(n*n/10);
    states.push_back({s1,s2});

    while(!q.empty()){
        auto [p,qv] = q.front(); q.pop();
        int id = pair_id[encode(p,qv)];
        for(int sym=0;sym<2;sym++){
            int pp = nxt[p][sym];
            int qq = nxt[qv][sym];
            ll code = encode(pp,qq);
            if(pair_id.find(code)==pair_id.end()){
                int nid = states.size();
                pair_id[code] = nid;
                states.push_back({pp,qq});
                q.push({pp,qq});
            }
        }
    }
    int M = states.size();

    // build paired-DFA transitions and initial-class
    vector<array<int,2>> pnxt(M);
    vector<int> init_class(M);
    for(int i=0;i<M;i++){
        auto [p,qv] = states[i];
        // transitions in pair-DFA
        for(int sym=0;sym<2;sym++){
            int pp = nxt[p][sym];
            int qq = nxt[qv][sym];
            pnxt[i][sym] = pair_id[encode(pp,qq)];
        }
        // classification: 0=reject,1=half,2=accept
        bool fp = is_final[p], fq = is_final[qv];
        init_class[i] = fp + fq; 
        // (0+0→0, 1+0 or 0+1→1, 1+1→2)
    }

    // 2) Hopcroft-like refinement: signature = (class, class[a], class[b])
    vector<int> cls = init_class, new_cls(M);
    while(true){
        // build signatures
        unordered_map<ll,int> sig_id;
        sig_id.reserve(M*2);
        int cnt = 0;
        for(int i=0;i<M;i++){
            // encode triple (cls[i], cls[pnxt[i][0]], cls[pnxt[i][1]]) into a 64-bit key
            ll key = ((ll)cls[i]<<42) ^ ((ll)cls[pnxt[i][0]]<<21) ^ cls[pnxt[i][1]];
            auto it = sig_id.find(key);
            if(it==sig_id.end()){
                sig_id[key] = cnt;
                new_cls[i] = cnt;
                cnt++;
            } else {
                new_cls[i] = it->second;
            }
        }
        if(new_cls == cls) break;
        cls.swap(new_cls);
    }
    // now cls[i] in [0..K-1)
    int K = *max_element(cls.begin(),cls.end()) + 1;

    // 3) build minimized automaton
    // pick a representative for each class
    vector<int> rep(K,-1);
    for(int i=0;i<M;i++){
        if(rep[cls[i]]<0) rep[cls[i]] = i;
    }
    // transitions on classes
    vector<array<int,2>> mnxt(K);
    for(int c=0;c<K;c++){
        int i = rep[c];
        mnxt[c][0] = cls[ pnxt[i][0] ];
        mnxt[c][1] = cls[ pnxt[i][1] ];
    }
    int s0 = cls[0]; // initial pair was id 0

    // 4) collect each acceptance class
    vector<int> R,H,A;
    R.reserve(K); H.reserve(K); A.reserve(K);
    for(int c=0;c<K;c++){
        int ic = init_class[ rep[c] ];
        if(ic==0)       R.push_back(c);
        else if(ic==1)  H.push_back(c);
        else            A.push_back(c);
    }

    // 5) output
    // #states
    cout << K << "\n";
    // transitions
    for(int i=0;i<K;i++){
        cout << i << " a " << mnxt[i][0] << "\n";
        cout << i << " b " << mnxt[i][1] << "\n";
    }
    // single start
    cout << s0 << "\n";
    // rejecting
    cout << R.size() << "\n";
    for(int x:R) cout << x << "\n";
    // half-accepting
    cout << H.size() << "\n";
    for(int x:H) cout << x << "\n";
    // accepting
    cout << A.size() << "\n";
    for(int x:A) cout << x << "\n";

    return 0;
}
