% This SWI-Prolog code has been developed by the (so far...) anonymous authors of the AAMAS2017 paper entitled "Decentralizing MAS Monitoring with DecAMon"
% It is made available with no warranties and, until information about AAMAS2017 acceptance/rejection, it can be neither used nor integrated into other pieces of software, apart from the AAMAS2017 reviewers for testing purposes only
%

% Example of use

% Run all tests and generate the tests.txt containing for each AIP :
% the execution time, the number of MSs found, ...
% ?- test. % (it could take some time)

% Run other tests
% ?- further_test. (it could take some time)

% Run a single test on a specific AIP
% ?- test(aip1).
% ?- test(aip2).
% ?- ...

% Find all Monitoring Safe partitions of an AIP
% ?- decAMon(aip1, MsPartitions).
% ?- decAMon(aip2, MsPartitions).
% ?- ...

% Find all Minimal Monitoring Safe partitions of an AIP
% ?- decAMon(aip1, MsPartitions), generate_MMS(MsPartitions, MMsPartitions).
% ?- decAMon(aip2, MsPartitions), generate_MMS(MsPartitions, MMsPartitions).
% ?- ...

% Find all Monitoring Safe partitions of an AIP (forcing some conditions to be preserved)
% only Monitoring Safe partitions of aip1 with alice and bob monitored together
% ?- decAMon(aip1, MsPartitions), together(MsPartitions, [alice, bob], Together).
% only Monitoring Safe partitions of aip1 with alice and bob monitored separately
% ?- decAMon(aip1, MsPartitions), disjoint(MsPartitions, [alice, bob], Disjoint).
% only Monitoring Safe partitions of aip6 with max partitions cardinality between 1 and 10
% ?- decAMon(aip6, MsPartitions), max_partition_cardinality_it(MsPartitions, 1, 10).
% only Monitoring Safe partitions of aip6 with max cardinality for each groups between 1 and 10
% ?- decAMon(aip6, MsPartitions), max_group_cardinality_it(MsPartitions, 1, 10).
% only Monitoring Safe partitions of aip6 with min singletons cardinality between 1 and 10
% ?- decAMon(aip6, MsPartitions), min_singleton_it(MsPartitions, 1, 10).

% Find a Monitoring Safe partition at time of an AIP
% ?- decOne(aip1, MsPartition).
% ?- decOne(aip2, MsPartition).
% ?- ...


:- use_module(library(coinduction)).
:- dynamic has_type/2.
:- coinductive pre_processing/3.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%   Predicates used by JAVA   %%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

decAMonJADE(MMsPartitions) :-
  trace_expression(T),
  decAMon(T, MsPartitions),
  generate_MMS(MsPartitions, MMsPartitions).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%




% next transition function (implements "delta" in the paper)
next(ET:T,E,T) :- has_type(E,ET).
next(T1\/_,E,T2) :- next(T1,E,T2).
next(_\/T1,E,T2) :- !,next(T1,E,T2).
next(T1|T2,E,T) :- next(T1,E,T3), fork(T3,T2,T).
next(T1|T2,E,T) :- !,next(T2,E,T4), fork(T1,T4,T).
next(T1*T2,E,T) :- next(T1,E,T3), concat(T3,T2,T).
next(T1*T2,E,T3) :- !,may_halt(T1), next(T2,E,T3).
next(T1 /\ T2,E,T) :- next(T1,E,T3), next(T2,E,T4), conj(T3,T4,T).  %%% conjunction
next(ET >> T1, E, ET >> T2)  :- event(E),(has_type(E,ET) *-> next(T1,E,T2); T2=T1).

% may_halt function (implements "empty" in the paper)
may_halt(epsilon) :- !.
may_halt(T1\/T2) :- (may_halt(T1), !; may_halt(T2)).
may_halt(T1|T2) :- !, may_halt(T1), may_halt(T2).
may_halt(T1*T2) :- !, may_halt(T1), may_halt(T2).
may_halt(T1/\T2) :- !, may_halt(T1), may_halt(T2).
may_halt(_>>T) :- !, may_halt(T).

does_not_halt(_:_).
does_not_halt(T1\/T2) :- !, does_not_halt(T1), does_not_halt(T2).
does_not_halt(T1|T2) :- (does_not_halt(T1), !; does_not_halt(T2)).
does_not_halt(T1*T2) :- (does_not_halt(T1), !; does_not_halt(T2)).
does_not_halt(T1/\T2) :- (does_not_halt(T1), !; does_not_halt(T2)).
does_not_halt(_>>T) :- !, does_not_halt(T).

%%% optimization
fork(epsilon, T, T) :- !.
fork(T, epsilon, T) :- !.
fork((T1l|T1r), T2, (T1l|(T1r|T2))) :- !.
fork(T1, T2, (T1|T2)).

concat(epsilon, T, T) :- !.
concat(T, epsilon, T) :- !.
concat((T1l*T1r), T2, T1l*(T1r*T2)) :- !.
concat(T1, T2, T1*T2).

conj(epsilon/\epsilon, epsilon) :- !.
conj((T1l/\T1r), T2, T1l/\(T1r/\T2)) :- !.
conj(T1, T2, T1/\T2).

% may_halt predicate iterated on the trace expression
% Example: T = msg1:epsilon, may_eventually_halt(T).
% Example: T = msg1:T, not(may_eventually_halt(T)).
may_eventually_halt(T) :-
  empty_assoc(A),
  may_eventually_halt(T, A).
may_eventually_halt(T, A) :-
  get_assoc(T, A, _), !, fail.
may_eventually_halt(T, _) :- may_halt(T), !.
may_eventually_halt(T, A) :-
  put_assoc(T, A, _, A1),
  next(T, _, T1), may_eventually_halt(T1, A1).

% involved_type unifies the second argument with the set containing all agents involved
% in the interaction type passed as first argument
% Example: involved_type(msg1, [alice, bob]).
involved_type(IntType, InvolvedAgents) :-
  findall(Agent, (has_type(msg(_, sender(Agent), _, _), IntType); has_type(msg(_, _, receiver(Agent), _), IntType)), Aux),
  list_to_set(Aux, InvolvedAgents).

% given a trace expression, involved unifies the second argument with the set
% containing all agents involved in the trace expression
% Example: involved((msg1:epsilon|msg2:epsilon), [alice, bob, charlie, david]).
involved(T, InvolvedAgents) :-
  empty_assoc(A),
  involved(T, InvolvedAgents, A).
% auxiliary predicate with the third argument to manage the coinduction
involved(T, [], A) :-
  get_assoc(T, A, _), !.
involved(epsilon, [], _). % no agents involved in empty trace expression
involved(IntType:T, InvolvedAgents, A) :-
  put_assoc(IntType:T, A, _, A1),
  involved(T, InvolvedT, A1),
  involved_type(IntType, InvolvedIntType), % get the agents involved in the interaction type
  union(InvolvedT, InvolvedIntType, InvolvedAgents). % do the union with the agents involved in T
involved(T1\/T2, InvolvedAgents, A) :- % propagation
  put_assoc(T1\/T2, A, _, A1),
  involved(T1, Involved1, A1),
  involved(T2, Involved2, A1),
  union(Involved1, Involved2, InvolvedAgents).
involved(T1|T2, InvolvedAgents, A) :- % propagation
  put_assoc(T1|T2, A, _, A1),
  involved(T1, Involved1, A1),
  involved(T2, Involved2, A1),
  union(Involved1, Involved2, InvolvedAgents).
involved(T1*T2, InvolvedAgents, A) :- % propagation
  put_assoc(T1*T2, A, _, A1),
  involved(T1, Involved1, A1),
  involved(T2, Involved2, A1),
  union(Involved1, Involved2, InvolvedAgents).
involved(T1/\T2, InvolvedAgents, A) :- % propagation
  put_assoc(T1/\T2, A, _, A1),
  involved(T1, Involved1, A1),
  involved(T2, Involved2, A1),
  union(Involved1, Involved2, InvolvedAgents).
involved(IntType>>T, InvolvedAgents, A) :- % propagation
  put_assoc(IntType>>T, A, _, A1),
  involved(T, InvolvedT, A1),
  involved_type(IntType, InvolvedIntType),
  union(InvolvedT, InvolvedIntType, InvolvedAgents).

% first_it unifies the second argument with the set of interaction types which are
% at the beginning of the trace expression (it starts with them)
% Example: first_it((msg1:epsilon|msg2:epsilon), [msg1, msg2]).
first_it(epsilon, []).
first_it(IntType:_, [IntType]).
first_it(T1\/T2, First) :-
  first_it(T1, First1),
  first_it(T2, First2),
  union(First1, First2, First).
first_it(T1|T2, First) :-
  first_it(T1, First1),
  first_it(T2, First2),
  union(First1, First2, First).
first_it(T1*T2, First) :-
  may_halt(T1), !,
  first_it(T1, First1),
  first_it(T2, First2),
  union(First1, First2, First).
first_it(T1*_, First) :-
  first_it(T1, First).
first_it(T1/\T2, First) :-
  first_it(T1, First1),
  first_it(T2, First2),
  union(First1, First2, First).

% last_it unifies the second argument with the set of interaction types which are
% at the end of the trace expression (it ends with them)
% Example: last_it((msg1:epsilon|msg2:msg3:epsilon), [msg1, msg3]]).
% Example: last_it(((msg1:epsilon)*(msg2:epsilon)), [msg2]]).
% Example: T = msg1:T, last_it(T, []).
last_it(T, Last) :-
  empty_assoc(A),
  last_it(T, Last, A).
% auxiliary version with the third argument to manage the cycles
last_it(T, [], A) :-
  get_assoc(T, A, _), !.
last_it(epsilon, [], _).
last_it(IntType:T, Last, A) :-
  may_halt(T), !,
  put_assoc(IntType:T, A, _, A1),
  last_it(T, LastT, A1),
  union([IntType], LastT, Last).
last_it(IntType:T, Last, A) :-
  put_assoc(IntType:T, A, _, A1),
  last_it(T, Last, A1).
last_it(T1\/T2, Last, A) :-
  put_assoc(T1\/T2, A, _, A1),
  last_it(T1, Last1, A1),
  last_it(T2, Last2, A1),
  union(Last1, Last2, Last).
last_it(T1|T2, Last, A) :-
  put_assoc(T1|T2, A, _, A1),
  last_it(T1, Last1, A1),
  last_it(T2, Last2, A1),
  union(Last1, Last2, Last).
last_it(T1*T2, Last, A) :-
  put_assoc(T1*T2, A, _, A1),
  last_it(T2, Last, A1).
last_it(T1/\T2, Last, A) :-
  put_assoc(T1/\T2, A, _, A1),
  last_it(T1, Last1, A1),
  last_it(T2, Last2, A1),
  union(Last1, Last2, Last).

% add_to_partitions adds each set in the first argument list to each set contained in the
% second argument list.
% Example: add_to_partitions([[a]], [[b], [c,d]], [[a, b], [a, c, d]]).
% Example: add_to_partitions([[a,e]], [[b], [c,d]], [[a, e, b], [a, e, c, d]]).
add_to_partitions([], Ps, Ps).
add_to_partitions([Set|[]], Ps, Res) :-
  !, findall(NewP, (member(P, Ps), union(Set, P, NewP)), Res).
add_to_partitions([Set|T], Ps, Res) :-
  add_to_partitions(T, Ps, Res1),
  findall(NewP, (member(P, Ps), union(Set, P, NewP)), Res2),
  union(Res1, Res2, Res).

% pre_processing phase necessary in order to remove >> operator
pre_processing(epsilon, epsilon, _).
pre_processing(IntType:T, IntType:TP, InvolvedAgents) :-
  pre_processing(T, TP, InvolvedAgents).
pre_processing(T1\/T2, TP1\/TP2, InvolvedAgents) :-
  pre_processing(T1, TP1, InvolvedAgents),
  pre_processing(T2, TP2, InvolvedAgents).
pre_processing(T1|T2, TP1|TP2, InvolvedAgents) :-
  pre_processing(T1, TP1, InvolvedAgents),
  pre_processing(T2, TP2, InvolvedAgents).
pre_processing(T1*T2, TP1*TP2, InvolvedAgents) :-
  may_eventually_halt(T1), !,
  pre_processing(T1, TP1, InvolvedAgents),
  pre_processing(T2, TP2, InvolvedAgents).
pre_processing(T1*_, TP1, InvolvedAgents) :-
  pre_processing(T1, TP1, InvolvedAgents).
pre_processing(T1/\T2, TP1/\TP2, InvolvedAgents) :-
  pre_processing(T1, TP1, InvolvedAgents),
  pre_processing(T2, TP2, InvolvedAgents).
pre_processing(IntType >> T, TP, InvolvedAgents) :-
  pre_processing(T, TP1, InvolvedAgents),
  findall(Msg, has_type(Msg, IntType), Interactions1),
  list_to_set(Interactions1, Interactions),
  findall(Msg, (has_type(Msg, _), Msg = msg(S,R,_), member(S, InvolvedAgents), member(R, InvolvedAgents)), World1),
  list_to_set(World1, World),
  subtract(World, Interactions, NotInteractions),
  string_concat(not, IntType, NotIntType),
  retractall(has_type(_, NotIntType)),
  asserta(has_type(Interaction, NotIntType) :- member(Interaction, NotInteractions)),
  T1 = (NotIntType:T1) \/ epsilon,
  T2 = IntType:T2,
  TP = (T1 | (T2 /\ TP1)).

% fuse_partitions fuses the sets contained in the first argument list
% Example: fuse_partitions([[a,b],[b]], [[a,b]]).
% Example: fuse_partitions([[a,b],[b],[c,d]], [[a,b],[c,d]]).
% Example: fuse_partitions([[a,b],[b],[c,d],[b,d]], [[a,b,c,d]]).
fuse_partitions([], []).
fuse_partitions([Set|T], Res) :-
  member(S, T),
  intersection(Set, S, I),
  I \== [], !,
  delete(T, S, T1),
  union(Set, S, Set1),
  fuse_partitions([Set1|T1], Res).
fuse_partitions([Set|T], [Set|Res]) :-
  fuse_partitions(T, Res).

% shuffle unifies the second argument with the shuffle of the elements contained
% in the first argument list
% Example: shuffle([[a,b],[c,d]], [a,b]). ;[b,c];[a,d];[b,d]
shuffle([], []).
shuffle([Set|T], [E|T1]) :-
  shuffle(T, T1),
  member(E, Set).

% distribute unifies the second argument with one among the many possible Minimal Monitoring-Safe
% partitions which can be used to distribute the monitoring process, preserving the global AIP semantics
distribute(T, Partition) :-
  empty_assoc(Assoc),
  involved(T, InvolvedAgents),
  findall([A], member(A, InvolvedAgents), InvolvedAgents1),
  distribute(T, Partition, Assoc, InvolvedAgents1).
distribute(T, Agents, Assoc, Agents) :-
  get_assoc(T, Assoc, _), !.
distribute(epsilon, Agents, _, Agents). % the empty protocol can be distributed on each agent
distribute(IntType:T, P, Assoc, Agents) :-
  not(get_assoc(T, Assoc, _)),
  involved_type(IntType, InvolvedAgents1),
  first_it(T, FirstIntTypes),
  findall(InvolvedAgents2, % find all agent sets with empty intersection with the agents involved in the interaction type
  (member(FirstIntType, FirstIntTypes),
   involved_type(FirstIntType, InvolvedAgents2),
   intersection(InvolvedAgents1, InvolvedAgents2, [])), InvolvedAgents2LAux),
  list_to_set(InvolvedAgents2LAux, InvolvedAgents2L),
  InvolvedAgents2L \== [], !, % the connectedness for sequence is not satisfied
  put_assoc(IntType:T, Assoc, _, Assoc1),
  distribute(T, P1, Assoc1, Agents),
  findall(Set, % check if the condition is already satisfied by the P1 partition
  (member(E1, InvolvedAgents1),
   shuffle(InvolvedAgents2L, S1),
   union([E1], S1, S2),
   member(Set, P1), subset(S2, Set))
  ,Dummy),
  ((Dummy \== []) ->
    (P = P1); % it is not necessary to add the constraint
    (
    member(A1, InvolvedAgents1),
    shuffle(InvolvedAgents2L, InvolvedAgents2a),
    union([A1], InvolvedAgents2a, InvolvedAgents2b),
    union([InvolvedAgents2b], P1, Ps), % add the new constraint
    fuse_partitions(Ps, P)
    )).
distribute(IntType:T, P, Assoc, Agents) :-
  put_assoc(IntType:T, Assoc, _, Assoc1),
  distribute(T, P, Assoc1, Agents).
distribute(T1\/T2, P, Assoc, Agents) :-
  first_it(T1, FirstIntTypes1),
  first_it(T2, FirstIntTypes2),
  % find all couples of set of agents with empty intersection
  findall((FirstInvolvedAgents1, FirstInvolvedAgents2),
  (member(FirstIntType1, FirstIntTypes1),
   member(FirstIntType2, FirstIntTypes2),
   involved_type(FirstIntType1, FirstInvolvedAgents1),
   involved_type(FirstIntType2, FirstInvolvedAgents2),
   intersection(FirstInvolvedAgents1, FirstInvolvedAgents2, [])
  ),
  Constraints),
  Constraints \== [], !, % the unique point of choice condition is not satisfied
  put_assoc(T1\/T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, Ps1),
  findall((C1,C2),
  (member((C1, C2), Constraints),
   member(E1, C1), member(E2, C2), member(Set, Ps1),
   subset([E1,E2], Set))
  ,Dummy),
  (subset(Constraints,Dummy) ->
    (P = Ps1);
    (
    subtract(Constraints, Dummy, Constraints1),
    findall(AgentsV,
      (member((C1, C2), Constraints1),
       findall([A1, A2],
        (member(A1, C1), member(A2, C2),
         findall(A3,
          (member(A3, C1), A3 \== A1, member(Set1, Ps1), member(Set2, Ps1),
           Set1 \== Set2,
           member(A3, Set1), member(A1, Set2), length(Set1, N1),
           length(Set2, N2), N1 > N2), []),
         findall(A3,
          (member(A3, C2), A3 \== A2, member(Set1, Ps1), member(Set2, Ps1),
           Set1 \== Set2,
           member(A3, Set1), member(A2, Set2), length(Set1, N1),
           length(Set2, N2), N1 > N2), [])),
       AgentsV)),
     SetConstraints),
     shuffle(SetConstraints, SetConstraint),
     union(SetConstraint, Ps1, P3),
     fuse_partitions(P3, P),
      findall((C1,C2), % check if redundant
       (member((C1, C2), Constraints1),
        ((member(A1, C1), member(A2, C2),
        member(A3, C1), member(A4, C2),
        A1 \== A3, A2 \== A4,
        member(Set1, P),
        subset([A1,A2], Set1),
        member(Set2, P),
        subset([A3,A4], Set2));
        ((member(A1, C1), member(A2, C1), A1 \== A2, member(Set, P), subset([A1,A2], Set));
         (member(A1, C2), member(A2, C2), A1 \== A2, member(Set, P), subset([A1,A2], Set)))))
      , [])
    )).
distribute(T1\/T2, P, Assoc, Agents) :-
  put_assoc(T1\/T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, P).
distribute(T1|T2, P, Assoc, Agents) :-
  put_assoc(T1|T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, P).
distribute(T1*T2, P, Assoc, Agents) :- % is the same as \/, with last and first instead of first and first
  last_it(T1, LastIntTypes),
  first_it(T2, FirstIntTypes),
  findall((LastInvolvedAgents, FirstInvolvedAgents),
  (member(LastIntType, LastIntTypes),
   member(FirstIntType, FirstIntTypes),
   involved_type(LastIntType, LastInvolvedAgents),
   involved_type(FirstIntType, FirstInvolvedAgents),
   intersection(LastInvolvedAgents, FirstInvolvedAgents, [])
  ),
  Constraints),
  Constraints \== [], !,
  put_assoc(T1*T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, Ps1),
  findall((C1,C2),
  (member((C1, C2), Constraints),
   member(E1, C1), member(E2, C2), member(Set, Ps1),
   subset([E1,E2], Set))
  ,Dummy),
  (subset(Constraints,Dummy) ->
    (P = Ps1);
    (
    subtract(Constraints, Dummy, Constraints1),
    findall(AgentsV,
      (member((C1, C2), Constraints1),
       findall([A1, A2],
        (member(A1, C1), member(A2, C2),
         findall(A3,
          (member(A3, C1), A3  \== A1, member(Set1, Ps1), member(Set2, Ps1),
           Set1 \== Set2,
           member(A3, Set1), member(A1, Set2), length(Set1, N1),
           length(Set2, N2), N1 > N2), []),
         findall(A3,
          (member(A3, C2), A3  \== A2, member(Set1, Ps1), member(Set2, Ps1),
           Set1 \== Set2,
           member(A3, Set1), member(A2, Set2), length(Set1, N1),
           length(Set2, N2), N1 > N2), [])),
       AgentsV)),
     SetConstraints),
     shuffle(SetConstraints, SetConstraint),
     union(SetConstraint, Ps1, P3),
     fuse_partitions(P3, P),
     findall((C1,C2),
      (member((C1, C2), Constraints1),
       ((member(A1, C1), member(A2, C2),
       member(A3, C1), member(A4, C2),
       A1 \== A3, A2 \== A4,
       member(Set1, P),
       subset([A1,A2], Set1),
       member(Set2, P),
       subset([A3,A4], Set2));
       ((member(A1, C1), member(A2, C1), A1 \== A2, member(Set, P), subset([A1,A2], Set));
        (member(A1, C2), member(A2, C2), A1 \== A2, member(Set, P), subset([A1,A2], Set)))))
     , [])
    )).
distribute(T1*T2, P, Assoc, Agents) :-
  put_assoc(T1*T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, P).
distribute(T1/\T2, P, Assoc, Agents) :-
  put_assoc(T1/\T2, Assoc, _, Assoc1),
  distribute(T1, P1, Assoc1, Agents),
  distribute(T2, P2, Assoc1, Agents),
  union(P1, P2, Ps),
  fuse_partitions(Ps, P).


%%% Utility and testing predicates %%%

% set of predicates for pretty printing the results of DecAMon
pretty_print_all_part([]).
pretty_print_all_part([H|T]) :-
   pretty_print(H),
   pretty_print_all_part(T).

pretty_print(P) :-
   write('\n{\n'),
   pretty_print_one_part(P),
   write('}\n').

pretty_print_one_part([]).
pretty_print_one_part([H|T]) :-
   T \= [],
   write('   {'), pretty_print_list(H), write('}, \n'),
   pretty_print_one_part(T).
pretty_print_one_part([H|[]]) :-
   write('   {'), pretty_print_list(H), write('}\n').

pretty_print_list([]).
pretty_print_list([H|T]) :-
   T \= [],
   write(H), write(', '),
   pretty_print_list(T).
pretty_print_list([H|[]]) :-
   write(H).

%%%%%%%%%%
% DecOne %
%%%%%%%%%%

decOne_print(T, P) :-
  involved(T, InvolvedAgents),
  pre_processing(T, Test, InvolvedAgents), !, distribute(Test, P), pretty_print(P).

decOne(T, P) :-
  involved(T, InvolvedAgents),
  pre_processing(T, Test, InvolvedAgents), !, distribute(Test, P).

%%%%%%%%%%%
% DecAMon %
%%%%%%%%%%%

decAMon(T, Ps) :-
  findall(P, decOne(T, P), Ps).

%%%%%%%%%%%%%%%%%%%%%%%%%%%
% DecAMon Post Processing %
%%%%%%%%%%%%%%%%%%%%%%%%%%%


generate_MMS(Ps, Partitions) :-
findall(P2,
    (member(P1, Ps),
     member(P2, Ps), P1\==P2,
    multi_subset(P1, P2)), Redundants),
  list_to_set(Redundants, Redundants1),
  %print(Redundants1),
  subtract(Ps, Redundants1, Partitions).

multi_subset([], _).
multi_subset([Set|T], SetofSets) :-
  member(Set1, SetofSets),
  subset(Set, Set1),
  multi_subset(T, SetofSets), !.

are_together(Agents, Partition) :-
   member(A, Agents),
   member(P, Partition),
   member(A, P),
   subset(Agents, P).


are_disjoint(Agents, Partition) :-
   member(A, Agents),
   member(B, Agents),
   A \= B,
   member(PA, Partition),
   member(A, PA),
   member(PB, Partition),
   member(B, PB),
   PA \= PB.

disjoint([], _Agents, []).
disjoint([H|T], Agents, [H|Res]) :-
   are_disjoint(Agents, H), !,
   disjoint(T, Agents, Res).
disjoint([_H|T], Agents, Res) :-
   disjoint(T, Agents, Res).


together([], _Agents, []).
together([H|T], Agents, [H|Res]) :-
   are_together(Agents, H), !,
   together(T, Agents, Res).
together([_H|T], Agents, Res) :-
   together(T, Agents, Res).


max_partition_cardinality_it(MMSs, Start, End) :-
   Start < End,
   max_partition_cardinality(MMSs, Start, MAX),
   length(MAX, LMAX),
   write('\nNumber of MMSs whose dimension is lesses or equal to '), write(Start), write(': '), write(LMAX),
   Start1 is Start+1,
   max_partition_cardinality_it(MMSs, Start1, End).

max_partition_cardinality_it(MMSs, End, End) :-
   max_partition_cardinality(MMSs, End, MAX),
   length(MAX, LMAX),
   write('\nNumber of MMSs whose dimension is lesses or equal to '), write(End), write(': '), write(LMAX), write('\n').

max_partition_cardinality([], _MaxCard, []).
max_partition_cardinality([H|T], MaxCard, [H|Res]):-
   length(H, L),
   L =< MaxCard,!,
   max_partition_cardinality(T, MaxCard, Res).
max_partition_cardinality([_H|T], MaxCard, Res):-
    max_partition_cardinality(T, MaxCard, Res).


max_group_cardinality_it(MMSs, Start, End) :-
   Start < End,
   max_group_cardinality(MMSs, Start, MAX),
   length(MAX, LMAX),
   write('\nNumber of MMSs whose largest group dimension is lesses or equal to '), write(Start), write(': '), write(LMAX),
   Start1 is Start+1,
   max_group_cardinality_it(MMSs, Start1, End).

max_group_cardinality_it(MMSs, End, End) :-
   max_group_cardinality(MMSs, End, MAX),
   length(MAX, LMAX),
   write('\nNumber of MMSs whose whose largest group dimension is lesses or equal to '), write(End), write(': '), write(LMAX), write('\n').

max_group_cardinality([], _MaxCard, []).
max_group_cardinality([H|T], MaxCard, [H|Res]):-
   max_group_dim(H, L),
   L =< MaxCard, !,
   max_group_cardinality(T, MaxCard, Res).
max_group_cardinality([_H|T], MaxCard, Res):-
   max_group_cardinality(T, MaxCard, Res).


max_group_dim([], 0).
max_group_dim(H, L) :-
   setof(Length, M^(member(M, H), length(M, Length)), Ls),
   max_list(Ls, L).
% max_group_dim(H, L) :-
%    setof(Length, M^(member(M, H), length(M, Length)), [L|_]).

min_singleton_it(MMSs, Start, End) :-
   Start < End,
   min_singleton(MMSs, Start, MAX),
   length(MAX, LMAX),
   write('\nNumber of MMSs whose number of singleton elements is greater or equal to '), write(Start), write(': '), write(LMAX),
   Start1 is Start+1,
   min_singleton_it(MMSs, Start1, End).

min_singleton_it(MMSs, End, End) :-
   min_singleton(MMSs, End, MAX),
   length(MAX, LMAX),
    write('\nNumber of MMSs whose number of singleton elements is greater or equal to '), write(End), write(': '), write(LMAX), write('\n').

min_singleton([], _Num, []).
min_singleton([H|T], Num, [H|Res]) :-
count_singleton_groups(H, Singleton),
Singleton >= Num, !,
min_singleton(T, Num, Res).
min_singleton([_H|T], Num, Res) :-
min_singleton(T, Num, Res).

count_singleton_groups(P, Singleton) :-
   count_singleton_groups(0, P, Singleton).
count_singleton_groups(Singleton, [], Singleton).
count_singleton_groups(Prev, [H|T], Singleton) :-
   (length(H, 1) ->
      (New is Prev+1,
       count_singleton_groups(New, T, Singleton));
     count_singleton_groups(Prev, T, Singleton)).



%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Testing Predicates   %
%%%%%%%%%%%%%%%%%%%%%%%%%%%

test :-
open('./tests.txt', append, Str),
tell(Str),
test(aip1),
flush_output(Str),
test(aip2),
flush_output(Str),
test(aip3),
flush_output(Str),
test(aip4),
flush_output(Str),
test(aip5),
flush_output(Str),
test(aip6),
flush_output(Str),
test(aip7),
flush_output(Str),
test(aip8),
flush_output(Str),
test(aip9),
flush_output(Str),
test(alt_bit_2),
flush_output(Str),
test(alt_bit_4),
flush_output(Str),
test(alt_bit_6),
told.

further_test :-
open('./further_tests.txt', append, Str),
tell(Str),
decAMon(aip1, Ps),
pretty_print_all_part(Ps),
together(Ps, [alice, bob], Together),
length(Together, L),
write('\n\nNumber of MMSs in aip1 where alice and bob are together: '),write(L), write('\n'),
flush_output(Str),
disjoint(Ps, [alice, bob], Disj),
length(Disj, D),
write('Number of MMSs in aip1 where alice and bob are disjoint: '),write(D), write('\n'),
flush_output(Str),
decAMon(aip5, Ps5),
together(Ps5, [b, c, d], Together5),
length(Together5, L5),
write('\n\nNumber of MMSs in aip5 where b, c, d are together: '),write(L5), write('\n'),
flush_output(Str),
disjoint(Ps5, [b, c, d], Disj5),
length(Disj5, D5),
write('Number of MMSs in aip5 where b, c, d are disjoint: '),write(D5), write('\n'),
flush_output(Str),
together(Ps5, [b, m], Together6),
length(Together6, L6),
write('Number of MMSs in aip5 where b, m are together: '),write(L6), write('\n'),
flush_output(Str),
disjoint(Ps5, [b, m], Disj6),
length(Disj6, D6),
write('Number of MMSs in aip5 where b, m are disjoint: '),write(D6), write('\n'),
flush_output(Str),
told.

test(PrName) :-
   write('\n\n**** '), write(PrName),  write(' ****\n'),
   statistics(walltime, [_ | [_]]),
   time(decAMon(PrName, Ps)),
   statistics(walltime, [_ | [ExecutionTime1]]),
   write('Execution of DecAMon took '), write(ExecutionTime1), write(' ms.'),
   length(Ps, L),
   write('\nNumber of MSs: '), write(L), write('\n'),
   statistics(walltime, [_ | [_]]),
   time(generate_MMS(Ps, MMSs)),
   statistics(walltime, [_ | [ExecutionTime2]]),
   write('Execution of generate_MMS took '), write(ExecutionTime2), write(' ms.'),
   length(MMSs, L1),
   write('\nNumber of MMSs: '), write(L1), write('\n'),
   %pretty_print_all_part(MMSs),
   max_partition_cardinality_it(MMSs, 1, 10),
   max_group_cardinality_it(MMSs, 1, 10),
   min_singleton_it(MMSs, 1, 10).