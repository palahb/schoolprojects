/*
In this project, negative cycle detection is implemented via considering the
concepts in the following link:
https://konaeakira.github.io/posts/using-the-shortest-path-faster-algorithm-to-find-negative-cycles.html
*/

#include <vector>
#include <queue>
#include <unordered_map>
#include <fstream>
#include <iostream>

using namespace std;

class Vertex{
public:
    int id;
    unordered_map<int,int> edges; // <neighbor,weight>
    Vertex(int id){
        this->id = id;
    }
};

//  This method is a modified version of the detect_cycle method in the link below:
//  https://konaeakira.github.io/assets/code-snippets/cycle-detection-with-spfa.cpp
bool detect_cycle(vector<Vertex>& graph, int n, int* pre, int* maxVoltage){
    vector<int> vec;
    bool visited[2*n], on_stack[2*n];
    fill(on_stack, on_stack+2*n, false);
    fill(visited, visited+2*n, false);
    for(int i = 0 ; i < 2*n ; i++){
        if(!visited[i]){
            for(int j = i ; j != -1; j = pre[j]){
                if(!visited[j]){
                    visited[j] = true;
                    vec.push_back(j);
                    on_stack[j] = true;
                } else{
                    if(on_stack[j]){
                        int x;
                        int pathWeight = 0;
                        for(x = 0 ; vec[x] != j ; x++);
                        for(int k = x ; k < vec.size() ; k++){
                            int v = vec[k];
                            int u;
                            if(k != vec.size()-1 ){
                                u = vec[k+1];
                            } else{
                                u = vec[x];
                            }
                            pathWeight += -1 * graph.at(u).edges[v];
                            int tempWeight = graph[u].edges[v];
                            graph[u].edges.erase(v);
                            graph[v].edges[u] = -1 * tempWeight;
                        }
                        *maxVoltage += pathWeight;
                        return true;
                    }
                    break;
                }
            }
            for(int j : vec){
                on_stack[j] = false;
                visited[j] = true;
            }
            vec.clear();
        }
    }
    return false;
}

//  This method is excerpted from the spfa_early_terminate method in the link below:
//  https://konaeakira.github.io/assets/code-snippets/cycle-detection-with-spfa.cpp
bool spfa_early_terminate(vector<Vertex>& graph, int n, int* maxVoltage){

    int dis[2*n], pre[2*n];
    bool in_queue[2*n];
    fill(dis, dis + 2*n, 0);
	fill(pre, pre + 2*n, -1);
	fill(in_queue, in_queue + 2*n, true);
	queue<int> queue;
    for(int i = 0 ; i < 2*n ; ++i){
        queue.push(i);
    }
    int iter = 0;
    while(!queue.empty()){
        int u = queue.front();
        queue.pop();
        in_queue[u] = false;
        for(auto edge : graph.at(u).edges){
            int v = edge.first, w = edge.second;
            if(dis[u] + w < dis[v]){
                pre[v] = u;
                dis[v] = dis[u] + w;
                iter++;
                if(iter == 2*n){
                    iter = 0;
                    if(detect_cycle(graph, n, pre, maxVoltage)){
                        return true;
                    }
                }
                if(!in_queue[v]){
                    queue.push(v);
                    in_queue[v] = true;
                }
            }
        }
    }
    if(detect_cycle(graph, n, pre, maxVoltage)){
        return true;
    }
    return false;
}

int main(int argc, char* argv[]) {

    string infile_name = argv[1];
	string outfile_name = argv[2];
	ifstream infile;
	infile.open(infile_name);
    ofstream outfile;
    outfile.open(outfile_name);

    int nofTestCases;

    infile >> nofTestCases;
    
    for(int i = 0 ; i < nofTestCases ; i++){
        vector<Vertex> graph;
        int n, maxVoltage = 0;
        infile >> n;
        graph.reserve(2*n);

        for(int j = 0 ; j < 2*n ; j++){
            Vertex v(j);
            graph.push_back(v);
        }
        for(int j = 0 ; j < n ; j++){
            for(int k = n ; k < 2*n ; k++){
                int weight;
                infile >> weight;
                if(k == n+j){
                    graph.at(k).edges[j] = weight;
                    maxVoltage += weight;
                }else{
                    graph.at(j).edges[k] = -1*weight;
                }   
            }
        }

        while(spfa_early_terminate(graph, n, &maxVoltage));

        outfile << maxVoltage << endl;
    }

    infile.close();
    outfile.close();

    return 0;
}
