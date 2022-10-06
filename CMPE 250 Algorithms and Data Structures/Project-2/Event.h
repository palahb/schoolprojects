#include "Hacker.h"
#include <iostream>
#include <string>

using namespace std;

#ifndef EVENT_H
#define EVENT_H

class Event{
    public:
        Hacker* hacker;
        float time;
        int commitLength;
        string type;
        Event(Hacker* hacker, float time, string type);
        Event(Hacker* _hacker, int _commitLength, float _time);
};

#endif