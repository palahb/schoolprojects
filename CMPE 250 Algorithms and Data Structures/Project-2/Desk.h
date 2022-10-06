#include <iostream>
using namespace std;

#ifndef DESK_H
#define DESK_H

class Desk{
    public:
        float waitingTime;
        bool isAvailable;
};

class StickerDesk : public Desk{
        static int nofStickerDesks;
    public:
        int ID;
        StickerDesk(float waitingTime);
        bool operator<(StickerDesk& other);
};

class HoodieDesk : public Desk{
        static int nofHoodieDesks;
    public:
        int ID;
        HoodieDesk(float waitingTime);
        bool operator<(HoodieDesk& other);
};

#endif