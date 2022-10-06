#include <iostream>
#include "Desk.h"
using namespace std;

#ifndef HACKER_H
#define HACKER_H

class Hacker{   
    public:
        static int nofHackers;
        float arrivalTime;
        float sQueueTime;
        float hQueueTime;
        float spentTimeinQs;
        int nofValidCommits;
        int nofGifts;
        int ID;
        Desk *desk;
        Hacker(float _arrivalTime);
        bool operator<(Hacker& other);
        Hacker& operator=(Hacker& other);
};

#endif