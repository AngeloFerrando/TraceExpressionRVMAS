match(alt_bit_4, msg(performative(inform), sender(alice), receiver(bob), content(msg1), _), msg1).
match(alt_bit_4, msg(performative(inform), sender(alice), receiver(bob), content(msg1), s), msg1_s).
match(alt_bit_4, msg(performative(inform), sender(alice), receiver(bob), content(msg1), r), msg1_r).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(mark), content(msgm1), _), msgm1).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(mark), content(msgm1), s), msgm1_s).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(mark), content(msgm1), r), msgm1_r).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(alice), content(ack1), _), ack1).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(alice), content(ack1), s), ack1_s).
match(alt_bit_4, msg(performative(inform), sender(bob), receiver(alice), content(ack1), r), ack1_r).
match(alt_bit_4, msg(performative(inform), sender(charlie), receiver(david), content(msg2), _), msg2).
match(alt_bit_4, msg(performative(inform), sender(charlie), receiver(david), content(msg2), s), msg2_s).
match(alt_bit_4, msg(performative(inform), sender(charlie), receiver(david), content(msg2), r), msg2_r).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(simon), content(msgm2), _), msgm2).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(simon), content(msgm2), s), msgm2_s).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(simon), content(msgm2), r), msgm2_r).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(charlie), content(ack2), _), ack2).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(charlie), content(ack2), s), ack2_s).
match(alt_bit_4, msg(performative(inform), sender(david), receiver(charlie), content(ack2), r), ack2_r).

match(alt_bit_4, Msg, msg) :-
  match(alt_bit_4, Msg, msg1); match(alt_bit_4, Msg, msg2).% match(alt_bit_4, Msg, msg3).
match(alt_bit_4, Msg, msg_ack_1) :-
  match(alt_bit_4, Msg, msg1); match(alt_bit_4, Msg, ack1).
match(alt_bit_4, Msg, msg_ack_2) :-
  match(alt_bit_4, Msg, msg2); match(alt_bit_4, Msg, ack2).
% match(alt_bit_4, Msg, msg_ack_3) :-
%   match(alt_bit_4, Msg, msg3); match(alt_bit_4, Msg, ack3).
% match(alt_bit_4, Msg, msgab) :-
%   match(alt_bit_4, Msg, msg1a); match(alt_bit_4, Msg, msg1b).
% match(alt_bit_4, Msg, msg_ack_1a) :-
%   match(alt_bit_4, Msg, msg1a); match(alt_bit_4, Msg, ack1a).
% match(alt_bit_4, Msg, msg_ack_1b) :-
%   match(alt_bit_4, Msg, msg1b); match(alt_bit_4, Msg, ack1b).

event(_).

reliable(alt_bit_4, msg1, 0.5).
reliable(alt_bit_4, msgm1, 1).
reliable(alt_bit_4, ack1, 1).
reliable(alt_bit_4, msg2, 1).
reliable(alt_bit_4, msgm2, 1).
reliable(alt_bit_4, ack2, 1).

trace_expression(alt_bit_4, T) :-
  T = (msg >> MM) /\ (msg_ack_1 >> MA1) /\ (msg_ack_2 >> MA2),
  MM = msg1:msg2:MM,
  MA1 = msg1:ack1:MA1,
  MA2 = msg2:ack2:MA2.
