#include "Event.h"

Event::Event(Hacker* _hacker, float _time, string _type){
    this->hacker = _hacker;
    this->time = _time;
    this->type = _type;
}

Event::Event(Hacker* _hacker, int _commitLength, float _time){
    this->commitLength = _commitLength;
    this->time = _time;
    this->hacker = _hacker;
    this->type = "CodeCommit";
}