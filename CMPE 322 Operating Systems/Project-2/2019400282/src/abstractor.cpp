/**
 * @file abstractor.cpp
 * @author Halil Burak Pala - 2019400282
 * @brief Implementation of the 2nd project of CMPE 322 in Fall 2021 @Bogazici University
 * @date January 2022
 * 
 * In this project, we are requried to design a program that analyzes some abstract files 
 * whose names are given in a file. In this file, some keywords are given and these keywords 
 * are inspected in the given abstract files. A Jaccard Similarity score is calculated for 
 * each abstract and requested number of most similar abstract files and summaries of them 
 * are printed to an output file. This program is a multithreaded program and aforementioned 
 * analyses are done by these threads.
 * 
 * How to compile and run:
 * g++ abstractor.cpp -lpthread -o abstractor.out
 * ./abstractor.out <path of the input file> <path of the output file>
 */

#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <pthread.h>
#include <string>
#include <algorithm>
#include <vector>
#include <map>
#include <queue>
#include <set>

using namespace std;

// This type will keep similarity score and the name of the abstract.
typedef pair<double, string> abstract; 

int nofAbstracts; // Number of abstracts to be inspected 
int nofResults;   // Number of results to be printed to the output file.
// nofAbstracts and nofResults are defined globally since they are
// needed in the thread function.

bool* assigned; // This is a boolean array which keeps track of whether each abstract is
                // assigned to a thread or not.
ofstream output;

vector<string> abstractNames;   // This vector will contain all of the names of the
                                // abstracts to be inspected.
set<string> query;  // This set will contain the query words.

priority_queue<abstract> abstracts; // This is a max heap which will contain abtracts
                                    // and their similarity score. I chose a max heap
                                    // because it keeps the ordering that we want.

// These are the mutexes that we will use.
pthread_mutex_t mtx1 = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mtx2 = PTHREAD_MUTEX_INITIALIZER;

string extractSummary(const string& abstractName);
double getSimilarity(const string& abstractName);
void* driver(void *args);

int main(int argc, char const* argv[]){
    
    if(argc != 3){ // If wrong number of command line arguments are given:
        cerr << "Correct usage: ./abstractor.out <input_file> <output_file>" << endl;
        return -1;
    }
    string inputFile = argv[1];
    string outFile = argv[2];

    ifstream input;
    input.open(inputFile);
    output.open(outFile);

    int nofThreads; // Number of threads to be created
    input >> nofThreads >> nofAbstracts >> nofResults;

    string queryWords; // Words to be queried in the abstracts
    getline(input,queryWords); // to eliminate new line from previous line
    getline(input,queryWords);
    
    stringstream X(queryWords);
    string queryWord;
    // Get every word to be queried and put into the set.
    while(getline(X, queryWord, ' ')){
        query.insert(queryWord);
    }

    // Get every abstract name requested to be inspected
    for(int i = 0 ; i < nofAbstracts; i++){
        string fileName;
        input >> fileName;
        abstractNames.push_back(fileName);
    }
    
    input.close();

    // Create enough memry space for our boolean array 'assigned'. It will contain
    // whether each abstract is claimed by a thread or not.
    assigned = (bool*) calloc(nofAbstracts, sizeof(bool));

    // Initialize this array. None of the abstracts is inspected yet. So, every 
    // item is false.
    for(int i = 0 ; i < nofAbstracts; i++){
        assigned[i] = false;
    }

    pthread_t* tids = (pthread_t *)calloc(nofThreads, sizeof(pthread_t)); // Thread ids

    for(int i = 0 ; i < nofThreads ; i++){
        long int threadName = 65+i; // This will be the thread name. Since there occur
        // loss of precision in case we use less precise type than long, I used long.
        // 65 is the ASCII value of the letter 'A'. Every thread will get its name by 
        // incrementing this value.

        if(pthread_create(tids+i, nullptr, &driver, (void*)threadName)!=0){
            cerr << "Error while creating thread" << endl;
        }
    }
    
    // Joining threads after analyses:
    for (int i = 0; i < nofThreads; i++){
        if(pthread_join(*(tids+i), nullptr)!=0){
            cerr << "Error while joining thread" << endl;
        }
    }

    output << "###" << endl;

    // Our 'abstracts' heap is a max heap. So, if we pop the first 'nofResults' items,
    // we can print what we are requested.
    for(int i = 1 ; i <= nofResults ; i++){
        abstract top = abstracts.top(); // 'top' is a pair whose first element is the
                                        // similarity score and second element is the
                                        // name of the abstract.
        abstracts.pop();
        string nameOfFile = top.second;
        double sim_score = top.first;
        string extractedSummary = extractSummary(nameOfFile); // Get the summary
        
        // Print the result to the output file
        output << "Result " << i << ":" << endl;
        output << "File: " << nameOfFile << endl;
        output << "Score: " << fixed << setprecision(4) << sim_score << endl;
        output << "Summary:" << extractedSummary << endl;
        output << "###" << endl;
    }

    output.close();
    return 0;
}
/**
 * @brief This function is the driver function for our threads. The analyses of abstracts
 * are done in this function.
 * 
 * @param args Thread names
 * @return void* None
 */
void* driver(void *args){
    // Every abstract should be assigned to a unique thread. This is checked by 'assigned' 
    // array. Following for loop is executed by every thread, i.e. every thread checks every
    // abstract to see if it is previously assigned by another thread or not.
    for(int i = 0 ; i < nofAbstracts ; i++){
        string abstractName;
        long int threadName = (long int)args;

        // Here, we need a mutex since every thread tries to write to 'abstractNames' heap and
        // tries to change 'assigned' array. To prevent race conditions, mutex is needed.
        pthread_mutex_lock(&mtx1);
        int currentAbstract = i;
        if(!assigned[currentAbstract]){
            abstractName = abstractNames.at(currentAbstract);
            output << "Thread " << (char)threadName << " is calculating " << abstractName << endl;
            assigned[currentAbstract] = true;
        }
        pthread_mutex_unlock(&mtx1);

        // If a thread cannot get any abstract,
        if(abstractName.empty()){
            // and if it is its last try, break the loop.
            if(currentAbstract == nofAbstracts-1) break;
            // If it is not its last try, it needs to continue to get an abstract.
            continue;
        }

        // Jaccard similarity score for an abstract
        double similarity_score = getSimilarity(abstractName);
        
        // Create a pair which contains the similartiy score and the name of the abstract
        // to push it into the max heap we created. 
        abstract abstractNameScore = pair<double,string>(similarity_score,abstractName);

        // Here, pushing to a common heap can also cause a race condition between
        // processes. So, a mutex is needed.
        pthread_mutex_lock(&mtx2);
        abstracts.push(pair<double,string>(similarity_score,abstractName));
        pthread_mutex_unlock(&mtx2);
    }
    return nullptr;
}

/**
 * @brief Extracts the summary of an abstract according to query words.
 * @param abstractName Name of the abstract whose summary is requested
 * @return The summary of the abstract
 */
string extractSummary(const string &abstractName){
    ifstream abstractFile;
    // abstracts are assumed to be given in the relative directory
    // ../abstracts/
    abstractFile.open("../abstracts/"+abstractName);

    // Getting sentences from the abstract
    vector<string> abstract_sentences;
    string word;
    string sentence = "";
    while(abstractFile){
        abstractFile >> word;
        sentence += " ";
        sentence += word;
        if(word != ".") continue;
        abstract_sentences.push_back(sentence);
        sentence = "";
    }

    abstractFile.close();

    // Extracting the summary of the abstract
    map<int,string> abs_summary_map; // To show sentences in the correct order in the summary
    string temp_str; // To prevent the same sentence appearing twice in the summary
    // of the abstract.
    set<string>::iterator query_itr;
    // Check for every query words:
    for(query_itr = query.begin() ; query_itr != query.end(); query_itr++){
        string queryWord = " ";
        queryWord += *query_itr;
        queryWord += " ";
        // For every sentence in the abstract, we want to check whether
        // at least one of the query words is present in that sentence.
        for(int i = 0 ; i < abstract_sentences.size() ; i++){
            // If we find the queried word in the sentence, find function
            // will return its index. If not, it will return string::npos
            string s = abstract_sentences[i];
            if(s.find(queryWord) != string::npos) {
                // To prevent writing same sentence more than once, we check
                // whether it is present in our extracted summary already. If
                // not, find function returns string::npos 
                if(temp_str.find(s) == string::npos){
                    temp_str.append(s);
                    abs_summary_map.insert(make_pair(i,s));
                }
            }
        }
    }

    string abs_summary_str; // This will be the extracted summary
    if(!abs_summary_map.empty()){
        // To print the summary in the correct order
        map<int,string>::iterator itr;
        for(itr = abs_summary_map.begin() ; itr != abs_summary_map.end() ; itr++){
            abs_summary_str += (*itr).second;
        }
    }
    return abs_summary_str;
}

/**
 * @brief Get the Jaccard similarity score of an abstract according to 
 * the given query words.
 * @param abstractName Name of the abstract whose similarity score is requested.
 * @return Jaccard similarity score of the abstract
 */
double getSimilarity(const string& abstractName){
    // Getting words from abstracts
    ifstream abstractFile;
    abstractFile.open("../abstracts/"+abstractName);
    set<string> abstract_words; 
    string word;
    while(abstractFile){
        abstractFile >> word;
        abstract_words.insert(word);
        if(word != ".") continue;
    }
    abstractFile.close();

    // Calculating the Jaccard similarity score:
    set<string> intersectionSet; // This set will contain the query words included in 
                                // the abstract
    // std::set_intersection method is used to get common words.
    set_intersection(abstract_words.begin(),abstract_words.end(), query.begin(), 
    query.end(), inserter(intersectionSet,intersectionSet.begin()));
    
    // Number of common words is size of the intesection set.
    int intersectionSize = (int)intersectionSet.size();

    // Union size is calculated by the formula:
    // s(A union B) = s(A) + s(B) - s(A intersection B) 
    int unionSize = (int)(abstract_words.size() + query.size()) - intersectionSize;

    // Jaccard similarity is the ratio of intersection size over union size.
    double similarity_score = (double)intersectionSize / (double)unionSize;

    return similarity_score;
}