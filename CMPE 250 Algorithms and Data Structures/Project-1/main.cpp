#include "Character.h"
#include <iostream>
#include <fstream>
#include <vector>
#include <stack>

using namespace std;

// author: Halil Burak Pala
// The implementation of the 1st Project of CMPE250, Fall 2020 @Bogazici University

vector<Character*> sort(vector<Character>& community);
Character* getCharacter(vector<Character*>& community, string name);
Character* getHobbit(vector<Character>& community);
bool isDefeated(vector<Character>& community);

int main(int argc, char* argv[]) {

    int nofMaxRounds, nofRounds = 0;
    string result = "Draw";
    vector<Character> community1;
    vector<Character> community2;

    stack<Character*> dead1;    // To store dead members of the community1
    stack<Character*> dead2;    // To store dead members of the community2

    string infile_name = argv[1];
    string outfile_name = argv[2];
    ifstream infile;
    infile.open(infile_name);

    infile >> nofMaxRounds;

    // Reading the members of community1 from the input file
    for(int i = 0 ; i < 5 ; i++){
        string name, type;
        int attack, defense, health;
        infile >> name >> type >> attack >> defense >> health;
        Character community1Character = Character(name, type, attack, defense, health, nofMaxRounds);
        community1.push_back(community1Character);
    }
    vector<Character*> community1Sorted = sort(community1); // Array of character pointers in the alphabetical order

    // Reading the members of community2 from the input file
    for(int i = 0 ; i < 5 ; i++){
        string name, type;
        int attack, defense, health;
        infile >> name >> type >> attack >> defense >> health;
        Character community2Character = Character(name, type, attack, defense, health, nofMaxRounds);
        community2.push_back(community2Character);
    }
    vector<Character*> community2Sorted = sort(community2); // Array of character pointers in the alphabetical order

    for(int round = 1 ; round <= nofMaxRounds ; round++){
        string attackerName, defenderName, isSpecial;
        infile >> attackerName >> defenderName >> isSpecial ;
        
        // In odd numbered rounds, attacker is from community1 and defender is from community2
        // In even numbered rounds, it is reversed.
        // If the requested attacker or defender is not alive, the logic narrated in the description
        // is applied via getCharacter function and corresponding character takes place of the dead character.
        Character *attacker = round % 2 == 1 ? getCharacter(community1Sorted, attackerName) : getCharacter(community2Sorted, attackerName);
        Character *defender = round % 2 == 1 ? getCharacter(community2Sorted, defenderName) : getCharacter(community1Sorted, defenderName);

        vector<Character>* communityOfAttacker = round % 2 == 1 ? &community1 : &community2; // To access the community of attacker
        int damage = attacker->attack - defender->defense;
        
        if(isSpecial == "SPECIAL"){ // If the character uses its special skill

            if(attacker->type == "Elves" && attacker->nRoundsSinceSpecial > 10){ 
                // If the type of the attacker which used its special skill is "Elf"
                // it conveys half of its health to the hobbit odf the community.
                // Its nRoundsSpecial is then set to -1 in order to be 0 at the next round. The same procedure 
                // is applied to other types as well.
                int conveyedHealth = attacker->remainingHealth / 2;
                attacker->remainingHealth = conveyedHealth;
                getHobbit(*communityOfAttacker)->remainingHealth += conveyedHealth;
                attacker->nRoundsSinceSpecial = -1;
            } else if(attacker->type == "Dwarfs" && attacker->nRoundsSinceSpecial > 20){
                // If the type of the attacker which used its special skill is "Dwarf"
                // it doubles the damage it has caused.
                if(damage > 0){
                    damage *= 2;
                }
                attacker->nRoundsSinceSpecial = -1;
            } else if(attacker->type == "Wizards" && attacker->nRoundsSinceSpecial > 50){
                // If the type of the attacker is "Wizard", the last died character is resurrected.
                // The resurrected character's nRoundsspecial is set to 0.
                if(communityOfAttacker == &community1){
                    if(!dead1.empty()){
                        Character* resurrected = dead1.top();
                        resurrected->isAlive = true;
                        resurrected->remainingHealth = resurrected->healthHistory[0];
                        resurrected->nRoundsSinceSpecial = 0;
                        dead1.pop();
                        attacker->nRoundsSinceSpecial = -1;
                    }
                } else if(communityOfAttacker == &community2){
                    if(!dead2.empty()){
                        Character* resurrected = dead2.top();
                        resurrected->isAlive = true;
                        resurrected->remainingHealth = resurrected->healthHistory[0];
                        resurrected->nRoundsSinceSpecial = 0;
                        dead2.pop();
                        attacker->nRoundsSinceSpecial = -1;
                    }
                }
            }
        }
        
        if(damage > 0){
            // If the damage is greater than 0, defender's health is decreased by this amount.
            // But if the health of the defender after the attack is 0 or below, then it means that
            // the attacker is died. Died caharacter's isAlive field is set to false and it is pushed 
            // to corresponding "dead" stack.  
            int tempHealth = defender->remainingHealth - damage;
            defender->remainingHealth = tempHealth < 0 ? 0 : tempHealth;
            if(defender->remainingHealth == 0){
                defender->isAlive = false;
                if(round % 2 == 1){
                    dead2.push(defender);
                }else{
                    dead1.push(defender);
                }
            }            
        }

        // Health history and nRoundsSinceSpecial of all the characters is updated.
        for(int i = 0 ; i < 5 ; i++){
            community1[i].nRoundsSinceSpecial++;
            community1[i].healthHistory[round] = community1[i].remainingHealth;
            community2[i].nRoundsSinceSpecial++;
            community2[i].healthHistory[round] = community2[i].remainingHealth;
        }

        nofRounds++;

        //If Community-1 is defeated, the winner is Community-2 and vice versa.
        if(isDefeated(community1)){
            result = "Community-2";
            break;
        } else if(isDefeated(community2)){
            result = "Community-1";
            break;
        }  
    }
    
    int nofCasualties = dead1.size() + dead2.size(); // # of casualties is the sum of the size of "dead" stacks.

    ofstream outfile;
    outfile.open(outfile_name);
    
    // Outputting the result
    outfile << result << "\n" << nofRounds << "\n" << nofCasualties << endl;

    for(int i = 0 ; i < 5 ; i++){
        outfile << community1[i].name << " ";
        for(int j = 0 ; j <= nofRounds ; j++){
            outfile << community1[i].healthHistory[j] << " ";
        }
        outfile << endl;
    }
    for(int i = 0 ; i < 5 ; i++){
        outfile << community2[i].name << " ";
        for(int j = 0 ; j <= nofRounds ; j++){
            outfile << community2[i].healthHistory[j] << " ";
        }
        outfile << endl;
    }

    infile.close();
    outfile.close();

    return 0;
}

vector<Character*> sort(vector<Character>& community){
    // This function creates a vector of Character pointers. This pointers are sorted in the
    // alphabetical order of the characters' names. 
    string* names = new string[5];
    for(int i = 0 ; i < 5 ; i++){
        names[i] = community[i].name;
    }
    int i, j, min_idx;    
    for (i = 0; i < 4; i++){   
        min_idx = i;  
        for (j = i+1; j < 5; j++){ 
            if (names[j] < names[min_idx]){  
                min_idx = j;  
            }
        }
        swap(names[min_idx], names[i]);  
    } 
    vector<Character*> communityPtrs;

    for(int i = 0 ; i < 5 ; i++){
        for(int j = 0 ; j < 5 ; j++){
            if(names[i] == community[j].name){
                communityPtrs.push_back(&community[j]);
                break;
            }
        }
    }

    delete[] names;
    return communityPtrs;
}

Character* getCharacter(vector<Character*>& community, string name){
    // This function is implemented in order to get the aplhabetically closest alive character 
    // according to the logic narrated in the description of the project.
    Character* ch;
    for(int i = 0 ; i < 5 ; i++){
        if(community[i]->name == name){
            if(community[i]->isAlive){
                return community[i];
            }else{
                for(int j = i+1 ; j < 5; j++){
                    if(community[j]->isAlive){
                        return community[j];
                    }
                }
                for(int j = i-1 ; j >= 0; j--){
                    if(community[j]->isAlive){
                        return community[j];
                    }
                }
            }
        }
    }
    return ch;
}

Character* getHobbit(vector<Character>& community){
    // This function returns a pointer to the hobbit of the corresponding community.
    Character* hobbit;
    for(int i = 0 ; i < 5 ; i++){
        if(community[i].type == "Hobbit"){
            hobbit = &community[i];
        }
    }
    return hobbit;
}

bool isDefeated(vector<Character>& community){
    // This function returns whether the given community is defeated or not. Defeat conditions
    // are narrated in the project description.
    Character* hobbit;
    hobbit = getHobbit(community);
    bool isHobbitAlive = hobbit->isAlive;
    bool areOthersAlive = false;
    for(int i = 0 ; i < 5 ; i++){
        if(community[i].type != "Hobbit"){
            areOthersAlive = areOthersAlive | community[i].isAlive; 
        }
    }
    if(isHobbitAlive && areOthersAlive){
        return false;
    } else{
        return true;
    }
}