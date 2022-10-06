#include "Desk.h"

int StickerDesk::nofStickerDesks = 0;
int HoodieDesk::nofHoodieDesks = 0;

StickerDesk::StickerDesk(float _waitingTime){
    nofStickerDesks++;
    this->isAvailable = true;
    this->waitingTime = _waitingTime;
    this->ID = nofStickerDesks;
}

bool StickerDesk::operator<(StickerDesk& other){
    return this->ID < other.ID;
}

HoodieDesk::HoodieDesk(float _waitingTime){
    nofHoodieDesks++;
    this->isAvailable = true;
    this->waitingTime = _waitingTime;
    this->ID = nofHoodieDesks;
}

bool HoodieDesk::operator<(HoodieDesk& other){
    return this->ID < other.ID;
}