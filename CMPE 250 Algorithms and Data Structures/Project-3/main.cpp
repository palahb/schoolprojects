#include <fstream>
#include <list>
#include <vector>
#include <queue>

using namespace std;

int nofEdges = 0, nofVertices;

class Vertex {
public:
	queue<int> edges;
	int outDegree;
	int inDegree;
	int ID;

	Vertex(int ID) {
		this->ID = ID;
		this->inDegree = 0;
		this->outDegree = 0;
	}

	bool hasNonUsedEdge(){
		return (edges.size() != 0);
	}

	int getFirstNonUsedEdge(){
		int e = edges.front();
		edges.pop();
		return e;
	}
};

bool isEulerian(vector<Vertex>& G){
	for(int i = 0 ; i < G.size() ; i++){
		int in = G.at(i).inDegree, out = G.at(i).outDegree;
		if(in != out){
			return false;
		}
	}
	return true;
}

void merge(list<int>& eulerianCircuit, list<int>& tour){
	int num = tour.back();
	list<int>::iterator it;
	for(it = eulerianCircuit.begin() ; it != eulerianCircuit.end() ; ++it){
		if(*it == num){
			++it;
			eulerianCircuit.insert(it, tour.begin(), tour.end());
			break;
		}
	}
}

Vertex* findFirstVertexinTheCircuitWithAnUnusedEdge(vector<Vertex>& G, list<int>& eulerianCircuit){
	list<int>::iterator it;
	Vertex* v = nullptr;
	for(it = eulerianCircuit.begin() ; it != eulerianCircuit.end() ; ++it){
		if(G.at(*it).hasNonUsedEdge()){
			v = &(G.at(*it));
			break;
		}
	}
	return v;
}

list<int> hierholzer(vector<Vertex>& G, Vertex* v){
	list<int> eulerianCircuit;
	if(!isEulerian(G)){
		return eulerianCircuit;
	}
	eulerianCircuit.push_back(v->ID);
	while(eulerianCircuit.size() <= nofEdges){
		list<int> tour;
		while(v->hasNonUsedEdge()){
			int e = v->getFirstNonUsedEdge();
			v = &(G.at(e));
			tour.push_back(v->ID);
		}
		merge(eulerianCircuit, tour);
		v = findFirstVertexinTheCircuitWithAnUnusedEdge(G, eulerianCircuit);
	}
	return eulerianCircuit;
}

int main(int argc, char const *argv[]){

    int startingID;
	list<int> eulerianCircuit;

	string infile_name = argv[1];
	string outfile_name = argv[2];
	ifstream infile;
	infile.open(infile_name);

	infile >> nofVertices;
	vector<Vertex> graph;
	graph.reserve(nofVertices);
	
	for(int i = 0 ; i < nofVertices ; i++){ 
		graph.push_back(Vertex(i));
	}

	for(int i = 0 ; i < nofVertices ; i++){
		int verticeID, outdegree;
		infile >> verticeID;
		infile >> outdegree;
		graph[i].outDegree = outdegree;
		for(int j = 0 ; j < outdegree; j++){
			nofEdges++;
			int e;
			infile >> e;
			graph[verticeID].edges.push(e); // cok zaman aliyor
			graph[e].inDegree++;
		}
	}

	infile >> startingID;

	Vertex* startingVertex = &(graph[startingID]);

	eulerianCircuit = hierholzer(graph, startingVertex);

	ofstream outfile;
    outfile.open(outfile_name);

	if(eulerianCircuit.size() == 0){
		outfile << "no path";
	} else{
		for(auto x : eulerianCircuit){
			outfile << x << " ";
		}
	}

	infile.close();
	outfile.close();

    return 0;
}