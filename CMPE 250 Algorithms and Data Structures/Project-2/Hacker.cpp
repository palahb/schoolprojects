#include "Hacker.h"

int Hacker::nofHackers = 0;

Hacker::Hacker(float _arrivalTime){
    nofHackers++;
    this->arrivalTime = _arrivalTime;
    this->ID = nofHackers;
    this->nofValidCommits = 0;
    this->nofGifts = 0;
    this->spentTimeinQs = 0;
    this->desk = nullptr;
}

bool Hacker::operator<(Hacker& other){
    return this->ID < other.ID;
}

Hacker& Hacker::operator=(Hacker &other){
    if(this == &other){
        return *this;
    }
    this->arrivalTime = other.arrivalTime;
    this->ID = other.ID;
    return *this;
}