#include "Hacker.h"
#include "Event.h"
#include "Desk.h"
#include <fstream>
#include <cmath>
#include <limits>
#include <vector>
#include <queue>

using namespace std;

struct CompareEventsByTime:binary_function<Event*, Event*, bool>{
    bool operator()(const Event* e1, const Event* e2){
        if(abs(e1->time - e2->time) < 0.00001){
            return e1->hacker > e2->hacker;
        }else{
            if(e1->time - e2->time >= 0.00001){
                return true;
            }
            else {
                return false;
            }
        }  
    }
};

struct CompareHackersByTime:binary_function<Hacker*, Hacker*, bool>{
    bool operator()(const Hacker* h1, const Hacker* h2){
        if(abs(h1->sQueueTime - h2->sQueueTime) < 0.00001){ 
            return h1 > h2;
        }else{
            if(h1->sQueueTime - h2->sQueueTime >= 0.0001)
                return true;
            else
                return false; 
        }
    }
};

struct CompareHackersByCommits:binary_function<Hacker*, Hacker*, bool>{
    bool operator()(const Hacker* h1, const Hacker* h2){
        if(h1->nofValidCommits == h2->nofValidCommits){
            if(abs(h1->hQueueTime - h2->hQueueTime) < 0.00001){
                return h1 > h2;
            } else{
                if(h1->hQueueTime - h2->hQueueTime >= 0.00001)
                    return true;
                else
                    return false;
            }
        } else{
            return h1->nofValidCommits < h2->nofValidCommits;
        }
    }
};


StickerDesk* availableStickerDesk(vector<StickerDesk*> desks){
    StickerDesk* desk = nullptr;
    for(int i = 0 ; i < desks.size() ; i++){
        if(desks[i]->isAvailable){
            desk = desks[i];
            break;
        }
    }
    return desk;
};

HoodieDesk* availableHoodieDesk(vector<HoodieDesk*> desks){
    HoodieDesk* desk = nullptr;
    for(int i = 0 ; i < desks.size() ; i++){
        if(desks[i]->isAvailable){
            desk = desks[i];
            break;
        }
    }
    return desk;
};

int main(int argc, char* argv[]) {

    int nofHackers, nofCommitEvents, nofQueueEntranceAttempts, nofStickerDesks, nofHoodieDesks;
    int maxLengthOfSQ = 0, maxLengthOfHQ = 0, totalNofGifts = 0, totalChangeLength = 0;
    int invalidQA = 0, invalidGifts = 0, hackerMostWaitedinQs, hackerLeastWaitedinQs = -1;
    float time = 0, totWaitinginSQ = 0, totWaitinginHQ = 0, totalTurnaroundTime = 0;
    float maxSpentTimeinQs = std::numeric_limits<float>::lowest();
    float minSpentTimeinQs = std::numeric_limits<float>::max();
    vector<Hacker*> hackers;
    priority_queue<Event*, vector<Event*>, CompareEventsByTime> events;
    vector<StickerDesk*> stickerDesks;
    vector<HoodieDesk*> hoodieDesks;
    priority_queue<Hacker*, vector<Hacker*>, CompareHackersByTime> stickerQueue;
    priority_queue<Hacker*, vector<Hacker*>, CompareHackersByCommits> hoodieQueue;

    string infile_name = argv[1];
    string outfile_name = argv[2];
    ifstream infile;
    infile.open(infile_name);

    infile >> nofHackers;

    for(int i = 0 ; i < nofHackers ; i++){
        float hackerArrivalTime;
        infile >> hackerArrivalTime;
        Hacker* hacker = new Hacker(hackerArrivalTime);
        hackers.push_back(hacker);
    }

    infile >> nofCommitEvents;

    for(int i = 0; i < nofCommitEvents ; i++){
        int hackerID, commitLength;
        float time;
        infile >> hackerID;;
        infile >> commitLength;
        infile >> time;
        Event* commit = new Event(hackers[hackerID-1], commitLength, time);
        events.push(commit);
    }

    infile >> nofQueueEntranceAttempts;

    for(int i = 0 ; i < nofQueueEntranceAttempts ; i++){
        int hackerID;
        float time;
        infile >> hackerID;
        infile >> time;
        Event* qe = new Event(hackers[hackerID-1], time, "QueueEntrance");
        events.push(qe);
    }

    infile >> nofStickerDesks ;

    for(int i = 0 ; i < nofStickerDesks ; i++){
        float waitingTime;
        infile >> waitingTime;
        StickerDesk* sd = new StickerDesk(waitingTime);
        stickerDesks.push_back(sd);
    }

    infile >> nofHoodieDesks ;

    for(int i = 0 ; i < nofHoodieDesks ; i++){
        float waitingTime;
        infile >> waitingTime;
        HoodieDesk* hd = new HoodieDesk(waitingTime);
        hoodieDesks.push_back(hd);
    }

    while(!events.empty()){

        Event* currentEvent = events.top();
        events.pop();
        time = currentEvent->time;
        Hacker* hacker = currentEvent->hacker;

        if(currentEvent->type == "CodeCommit"){
            if(currentEvent->commitLength >= 20){
               hacker->nofValidCommits++;
            }
            totalChangeLength += currentEvent->commitLength;
        }

        else if(currentEvent->type == "QueueEntrance"){
            if(hacker->nofValidCommits < 3){
                invalidQA++;
            } else if(hacker->nofGifts >= 3){
                invalidGifts++;
            } else{
                hacker->sQueueTime = time;
                stickerQueue.push(hacker);
            }
        }

        else if(currentEvent->type == "LeaveStickerDesk"){
            hacker->desk->isAvailable = true;
            hacker->desk = nullptr;
            hacker->hQueueTime = time;
            hoodieQueue.push(hacker);
        }

        else if(currentEvent->type == "LeaveHoodieDesk"){
            hacker->desk->isAvailable = true;
            hacker->desk = nullptr;
            totalTurnaroundTime += (time - hacker->sQueueTime);
            totalNofGifts++;
            hacker->sQueueTime = 0;
            hacker->hQueueTime = 0;
            hacker->nofGifts++;
        }
      
        if(stickerQueue.size() != 0){
            StickerDesk* availableSD = availableStickerDesk(stickerDesks);
            if(availableSD != nullptr){
                Hacker* hacker = stickerQueue.top();
                stickerQueue.pop();
                hacker->desk = availableSD;
                availableSD->isAvailable = false;
                hacker->spentTimeinQs += (time - hacker->sQueueTime);
                totWaitinginSQ += (time - hacker->sQueueTime);
                Event* leaveSD = new Event(hacker, time + availableSD->waitingTime, "LeaveStickerDesk");
                events.push(leaveSD);
            }
        }

        if(stickerQueue.size() > maxLengthOfSQ){
            maxLengthOfSQ = stickerQueue.size();
        }

        if(hoodieQueue.size() != 0){
            HoodieDesk* availableHD = availableHoodieDesk(hoodieDesks);
            if(availableHD != nullptr){
                Hacker* hacker = hoodieQueue.top();
                hoodieQueue.pop();
                hacker->desk = availableHD;
                availableHD->isAvailable = false;
                hacker->spentTimeinQs += (time - hacker->hQueueTime);
                
                totWaitinginHQ += (time - hacker->hQueueTime);
                Event* leaveHD = new Event(hacker, time + availableHD->waitingTime, "LeaveHoodieDesk");
                events.push(leaveHD);
            }
        }

        if(hoodieQueue.size() > maxLengthOfHQ){
            maxLengthOfHQ = hoodieQueue.size();
        }
    }

    bool check = false;

    for(auto hacker : hackers){
        if(hacker->spentTimeinQs - maxSpentTimeinQs >= 0.0001){
            maxSpentTimeinQs = hacker->spentTimeinQs;
            hackerMostWaitedinQs = hacker->ID;
        }

        if(minSpentTimeinQs - hacker->spentTimeinQs >= 0.00001 && hacker->nofGifts >= 3){
            check = true;
            minSpentTimeinQs = hacker->spentTimeinQs;
            hackerLeastWaitedinQs = hacker->ID;
        }
    }


    if(!check){
        minSpentTimeinQs = -1;
    }

    ofstream outfile;
    outfile.open(outfile_name);

    outfile.precision(3);

    outfile << maxLengthOfSQ << endl;
    outfile << maxLengthOfHQ << endl;
    outfile << fixed << (float)totalNofGifts / nofHackers << endl;
    outfile << fixed << (float)totWaitinginSQ / totalNofGifts << endl;
    outfile << fixed << (float)totWaitinginHQ / totalNofGifts << endl; //
    outfile << fixed << (float)nofCommitEvents / nofHackers << endl;
    outfile << fixed << (float)totalChangeLength / nofCommitEvents << endl;
    outfile << fixed << (float)totalTurnaroundTime / totalNofGifts << endl; //
    outfile << invalidQA << endl;
    outfile << invalidGifts << endl;
    outfile << fixed << hackerMostWaitedinQs << " ";
    outfile << maxSpentTimeinQs << endl; //
    outfile << fixed << hackerLeastWaitedinQs << " ";
    outfile << minSpentTimeinQs << endl;
    outfile << time;

    infile.close();
    outfile.close();

    return 0;
}