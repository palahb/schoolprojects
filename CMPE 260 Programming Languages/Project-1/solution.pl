% halil burak pala
% 2019400282
% compiling: yes
% complete: yes

% include the knowledge base
:- ['load.pro'].

% 3.1 glanian_distance(Name1, Name2, Distance) 5 points
% 3.2 weighted_glanian_distance(Name1, Name2, Distance) 10 points
% 3.3 find_possible_cities(Name, CityList) 5 points
% 3.4 merge_possible_cities(Name1, Name2, MergedCities) 5 points
% 3.5 find_mutual_activities(Name1, Name2, MutualActivities) 5 points
% 3.6 find_possible_targets(Name, Distances, TargetList) 10 points
% 3.7 find_weighted_targets(Name, Distances, TargetList) 15 points
% 3.8 find_my_best_target(Name, Distances, Activities, Cities, Targets) 20 points
% 3.9 find_my_best_match(Name, Distances, Activities, Cities, Targets) 25 points

% subtract_two_list(+List1, +List2, -List3) : This predicate subtracts two lists 
% List1 and List2 element by element and put the results into List3. If the minuend i
% n this subtraction is -1, the result of the operation is 0 regardless of what subtrahend is.
subtract_two_list([], [], []).
subtract_two_list([H1|T1], [H2|T2], [HeadResult|TailResult]) :-
    subtract_two_list(T1,T2,TailResult),
    ((H1 \= -1, HeadResult is H1-H2, !);
    (H1 = -1, HeadResult is 0)).

% square_list(+List1, -List2) : This predicate squares the elements of List1 and put
% the results into List2.
square_list([],[]).
square_list([H|T], Result) :-
    square_list(T, TailResult),
    HeadResult is H*H,
    Result = [HeadResult|TailResult].

% multiply_two_list(+List1, +List2, -List3) : This predicate multiplies the lists List1
% and List2 element by element and put the results into List3. If the multiplier is -1,
% result will be 0 regardless of what multiplicand is.
multiply_two_list([],[],[]).
multiply_two_list([H1|T1], [H2|T2], [HeadResult|TailResult]) :-
    multiply_two_list(T1,T2,TailResult),
    ((H1 \= -1, HeadResult is H1*H2, !);
    (H1 = -1, HeadResult is 0)).

% 3.1 glanian_distance(+Name1, +Name2, -Distance): Given two glanians Name1 and Name2, this 
% predicate will return the distance from Name1 to Name2. This distance is the Euclidean 
% distance between Name1’s ExpectedFeatures and Name2’s Glanian- Features. It is a measure 
% of how closer Name2 to Name1’s expectations.
glanian_distance(Name1, Name2, Distance) :-
    expects(Name1,_,E), % E is the expectations list of Name1. Let E=[e1,e2,...]
    glanian(Name2,_,F), % F is the features list of Name2. Let F=[f1,f2,...]
    subtract_two_list(E,F,Subtracted), % Subtracted=[a1,a2,...]=[e1-f1,e2-f2,...] (if en==-1, then an=0)
    square_list(Subtracted, Squared), % Squared=[b1,b2,...]=[a1^2,a2^2,...]
    sum_list(Squared, Sum), % Sum=b1+b2+...
    Distance is sqrt(Sum). % Distance=sqrt(Sum)

% 3.2 weighted_glanian_distance(+Name1, +Name2, -Distance): Given two glanians Name1 and Name2, 
% this predicate will return the weighted distance from Name1 to Name2. It is almost the same 
% as the first except that Name1 puts weights to each features, showing her preferences.
weighted_glanian_distance(Name1, Name2, Distance) :-
    expects(Name1,_,E), % E is the expectations list of Name1. Let E=[e1,e2,...]
    weight(Name1, W), % W is the weights list of Name1. Let W=[w1,w2,...]
    glanian(Name2,_,F), % F is the features list of Name2. Let F=[f1,f2,...]
    subtract_two_list(E,F,Subtracted), % Subtracted=[a1,a2,...]=[e1-f1,e2-f2,...] (if en==-1, then an=0)
    square_list(Subtracted, Squared), % Squared=[b1,b2,...]=[a1^2,a2^2,...]
    multiply_two_list(Squared, W, Weighted), % Weighted=[c1,c2,...]=[w1*b1,w2*b2,...] (if wn==-1, then cn=0)
    sum_list(Weighted, Sum), % Sum=c1+c2+...
    Distance is sqrt(Sum). % Distance=sqrt(Sum)

% current_city(+Name, -City): Given the glanian Name, this predicate will return the current
% city of this Name in City.
current_city(Name, City) :-
    city(X,Y,_),
    member(Name,Y),
    City = X, !.

% 3.3 find_possible_cities(+Name, -CityList): This predicate will return a list of cities 
% which contains the current city of Name and Name's LikedCities.
find_possible_cities(Name, CityList) :-
    current_city(Name, CurrentCity),
    likes(Name,_,LikedCities),
    CityList = [CurrentCity|LikedCities].

% 3.4 merge_possible_cities(+Name1, +Name2, -MergedCities): Given two glanians Name1 and 
% Name2, this predicate will return the union of the two glanian’s possible cities.    
merge_possible_cities(Name1, Name2, MergedCities) :-
    find_possible_cities(Name1, CityList1),
    find_possible_cities(Name2, CityList2),
    union(CityList1, CityList2, MergedCities).

% 3.5 find_mutual_activities(+Name1, +Name2, -MutualActivities): Given two glanians Name1 
% and Name2, this predicate will return the list of mutual activities of two glanians. An 
% activity is mutual if it is in both glanians’ LikedActivities.
find_mutual_activities(Name1, Name2, MutualActivities) :-
    likes(Name1, Activities1, _), % Activities1 is the LikedActivities of Name1
    likes(Name2, Activities2, _), % Activities2 is the LikedActivities of Name2
    intersection(Activities1, Activities2, MutualActivities). % Taking the intersection of them
    % will give the mutual activities.

% divide_dashed_list2(?List1, ?List2, ?List3): Divides the given dashed list into two
% seperate lists. e.g. List1=[1-a,2-b,3-c], List2=[1,2,3], List3=[a,b,c]
divide_dashed_list2([], [] ,[]).
divide_dashed_list2([Head|Tail], [FirstHead|FirstTail], [SecondHead|SecondTail]) :-
    Head = FirstHead-SecondHead,
    divide_dashed_list2(Tail, FirstTail, SecondTail).

% 3.6 find_possible_targets(+Name, -Distances, -TargetList): This predicate will return a 
% list of possible glanians sorted by their distances as possible matching targets for Name. 
% Distances is a sorted list that contains glanian distance from Name to glanians in 
% TargetList. The gender of each glanian in TargetList is in ExpectedGenders of Name. 
% For example, if a glanian has an empty list in her expects entry, then the results is 
% an empty list since she does not expect any gender.
find_possible_targets(Name, Distances, TargetList) :-
    expects(Name,Genders,_), % Genders is the expected genders of Name.
    % We will find all glanians whose gender is in Genders list and their glanian_distance to Name:
    findall( 
        Distance-Glanian, 
        (
            glanian(Glanian,Gender,_), 
            member(Gender,Genders), 
            Glanian \= Name,
            glanian_distance(Name, Glanian, Distance)
        ), 
        UnsortedPairs), % After findall, Pairs will be unsorted.
    keysort(UnsortedPairs, SortedPairs), % We will sort them according to Distances.
    divide_dashed_list2(SortedPairs, Distances, TargetList). % Then divide pairs into Distances and TargetList.

% 3.7 find_weighted_targets(+Name, -Distances, -TargetList): This predicate will return a list 
% of possible glanians sorted by their weighted distances as possible matching targets for Name. 
% It is the same as the previous predicate except that the distances are calculated with weighted 
% glanian distance(Name, Target, D).
find_weighted_targets(Name, Distances, TargetList) :-
    expects(Name,Genders,_), % Genders is the expected genders of Name.
    % We will find all glanians whose gender is in Genders list and their 
    % weighted_glanian_distance to Name:
    findall(
        Distance-Glanian, 
        (
            glanian(Glanian,Gender,_), 
            member(Gender,Genders),
            Glanian \= Name, 
            weighted_glanian_distance(Name, Glanian, Distance)
        ), 
        UnsortedPairs), % After findall, Pairs will be unsorted.
    keysort(UnsortedPairs, SortedPairs), % We will sort them according to Distances.
    divide_dashed_list2(SortedPairs, Distances, TargetList). % Then divide pairs into Distances and TargetList.

% between_limits(+Limits, +Elements): This predicate returns true if a given element in Elements
% list is between the corresponding limits in Limits list. For example: between_limits([[1,4],[2,5]],[3,5])
% is true since 1 <= 3 <= 4 and 2 <= 5 <=5. 
between_limits([],[]).
between_limits([LimitsHead|LimitsTail], [FeaturesHead|FeaturesTail]) :-
    between_limits(LimitsTail, FeaturesTail),
    ((LimitsHead = [], LowerLimit=0, UpperLimit=1); 
    (LimitsHead = [LowerLimit,UpperLimit])),
    LowerLimit < FeaturesHead, FeaturesHead < UpperLimit.   

% features_between_limits(+Name, +Target): This predicate returns true if a given feature in Target's Features
% list is between the corresponding limits in Name's Limits list.
features_between_limits(Name, Target) :-
    dislikes(Name,_,_,[LimitsHead|LimitsTail]),
    glanian(Target, _, [FeaturesHead|FeaturesTail]),
    between_limits([LimitsHead|LimitsTail], [FeaturesHead|FeaturesTail]).

% divide_dashed_list4(?List1, ?List2, ?List3, ?List3, ?List4, ?List5): Divides the given dashed list into four
% seperate lists. e.g. List1=[1-a-x-k,2-b-y-l,3-c-z-m], List2=[1,2,3], List3=[a,b,c], List4=[x,y,z], List5=[k,l,m]
divide_dashed_list4([], [] ,[], [], []).
divide_dashed_list4([Head|Tail], [FirstHead|FirstTail], [SecondHead|SecondTail], [ThirdHead|ThirdTail], [FourthHead|FourthTail]) :-
    Head = FirstHead-SecondHead-ThirdHead-FourthHead,
    divide_dashed_list4(Tail, FirstTail, SecondTail, ThirdTail, FourthTail).

% is_compatible(+Name, +Activity, +City): This predicate is returns whether given Name, Activity and City
% is compatible according to this rule:
% ((Activity is in City) AND (City is in find_possible_cities(Name) OR (Activity is in LikedActivities)))
% AND (Activity is not in DislikedActivities) AND (City is not in DislikedCities)
% These rules are constructed according to predicate 3.8's 2nd, 3rd and 4th conditions.
is_compatible(Name,Activity,City) :-
    find_possible_cities(Name, CityList),
    city(City,_,ActivityList),
    likes(Name,LikedActivities,_),
    dislikes(Name,DislikedActivities,DislikedCities,_),

    (member(Activity,ActivityList),
    (member(City,CityList);member(Activity,LikedActivities))),
    \+member(Activity,DislikedActivities),
    \+member(City,DislikedCities).

% target_check(+Name, +Target): This predicate checks whether Target is an appropriate target for
% Name according to predicate 3.8's 6th, 7th and 8th conditions.
target_check(Name,Target) :-
    glanian(Target,Gender,_), 
    expects(Name, ExpectedGenders, _),
    member(Gender, ExpectedGenders), % 3.8.6: Target’s gender should be in the expected gender list of Name.
    features_between_limits(Name,Target), % 3.8.7: Target’s features should be in the tolerance limits of Name.
    dislikes(Name,DislikedActivities,_,_),
    likes(Target,LikedActivities,_),
    intersection(DislikedActivities,LikedActivities,Intersection),
    length(Intersection,Length),    % 3.8.8: The intersection between Name’s DislikedActivities and Target’s
    Length=<2.                      % LikedActivities should not be more than two. 

% 3.8 find_my_best_target(+Name, -Distances, -Activities, -Cities, -Targets): This predicate will 
% use all the other restrictions to find possible matching targets together with possible 
% activities in possible cities. So in the end, a glanian will enter her name and will get a 
% list of distances to her matching targets, and activities that can be done in possible cities.
% We can read each element in four of these lists as follows:
% “Name and TargetList[i] can do ActivityList[i] in City[i]. This matching is close to the Name’s 
% preferences by Distances[i].”
find_my_best_target(Name, Distances, Activities, Cities, Targets) :- 
    findall(
        Distance-Activity-City-Target, % We are searching for four-tuples (Distance-Activity-City-Target)
        (
            % Following two lines checks the condition 3.8.1.
            \+old_relation([Name-Target]), 
            \+old_relation([Target-Name]),  
            % Following line checks the conditions 3.8.2-3-4.
            is_compatible(Name,Activity,City),
            % Following line checks the condiitons 3.8.6-7-8.
            target_check(Name,Target),
            % Following line checks the condition 3.8.5.
            merge_possible_cities(Name, Target, CityList),
            member(City,CityList),
            % Following line calculates the Distance using weighted_glanian_distance.
            weighted_glanian_distance(Name,Target,Distance)

        ),
        UnsortedPairs % After all checks, we will get unsorted four-tuples.
    ),
    keysort(UnsortedPairs,SortedPairs), % Then we sort them according to Distance's.
    list_to_set(SortedPairs, PairList), % To eliminate the same results in the list , we turn the list to a set
    divide_dashed_list4(PairList,Distances,Activities,Cities,Targets). % After all of them, we split these four-tuples.

% 3.9 find_my_best_match(+Name, -Distances, -Activities, -Cities, -Targets): This predicate is similar 
% to the previous predicate with some additional constraints. In this pred- icate, we also take 
% the matching target’s preferences into account.
find_my_best_match(Name, Distances, Activities, Cities, Targets) :- 
    findall(
        Distance-Activity-City-Target, % We are searching for four-tuples (Distance-Activity-City-Target)
        (
            % Following two lines checks the condition 3.9.1.
            \+old_relation([Name-Target]),
            \+old_relation([Target-Name]),
            % Following line checks the conditions 3.9.2-4-5.
            is_compatible(Name,Activity,City),
            % Following line checks the conditions 3.9.7-9-11.
            target_check(Name,Target),
            % Following line checks the conditions 3.9.3-4-5.
            is_compatible(Target,Activity,City),
            % Following line checks the conditions 3.9.8-10-12
            target_check(Target,Name),
            % Following line calculates the Distance using the formula
            % (wgd(Name,Target,Distance)+wgd(Target,Name,Distance))/2
            weighted_glanian_distance(Name,Target,Distance1),
            weighted_glanian_distance(Target,Name,Distance2),
            Distance is (Distance1+Distance2)/2
        ),
        UnsortedPairs % After all checks, we will get unsorted four-tuples.
    ),
    keysort(UnsortedPairs,SortedPairs), % Then we sort them according to Distance's.
    list_to_set(SortedPairs, PairList), % To eliminate the same results in the list , we turn the list to a set
    divide_dashed_list4(PairList,Distances,Activities,Cities,Targets). % After all of them, we split these four-tuples.


