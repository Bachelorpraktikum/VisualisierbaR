grammar Log;

start: (elements NEWLINE)+;

elements:         node
                | edge
                | elem
                | train
                | mv_init
                | mv_leaves
                | mv_speed
                | mv_start
                | mv_term
                | ch
                | data;

node: 'NODE' SEP node_name SEP coord;

edge: 'EDGE' SEP edge_name SEP node_name SEP node_name SEP INT;

elem: 'ELEM' SEP elem_name SEP node_name SEP STATE;

train: 'ZUG' SEP train_name SEP train_readable_name SEP INT;

mv_init: MV_IND SEP 'INIT' SEP train_name SEP time SEP edge_name;

mv_speed: MV_IND SEP 'SPEED' SEP train_name SEP time SEP distance (SEP speed)?;

mv_start: MV_IND SEP 'REACHSTART' SEP train_name SEP time SEP edge_name SEP distance;

mv_leaves: MV_IND SEP 'LEAVES' SEP train_name SEP time SEP edge_name SEP distance;

mv_term: MV_IND SEP 'TERM' SEP train_name SEP time SEP distance;

ch: 'CH' SEP elem_name SEP STATE SEP time;

data: 'DATA' SEP train_readable_name SEP time WHITESPACE speed SEP time_with_wrapper;

MV_IND: 'MV';

node_name: name;
edge_name: name;
train_name: name;
speed: INT;
distance: INT;
train_readable_name: (WORD | INT)+;
elem_name: name;
name: name_prefix (WORD+ | UNDERSCORE)+;
coord: INT SEP INT;
name_prefix: '<' INT DOT INT DOT INT '>' COLON;
time: RAT | INT;
time_with_wrapper: 'Time' '(' time ')';


STATE:   NOSIG
       | FAHRT
       | STOP
       ;
NOSIG: 'NOSIG';
FAHRT: 'FAHRT';
STOP: 'STOP';
INT: DIGIT+;
RAT: INT '/' INT;
WORD: (LOWERCASE_CHAR | UPPERCASE_CHAR)+;
LOWERCASE_WORD: LOWERCASE_CHAR+;
UPPERCASE_WORD: UPPERCASE_CHAR+;
LOWERCASE_CHAR: ('a' .. 'z');
UPPERCASE_CHAR: ('A' .. 'Z');
DIGIT: ('0' .. '9');
DOT: '.';
SEP: ';';
COLON: ':';
UNDERSCORE: '_';
NEWLINE: '\n' | '\r' | '\r\n';
WHITESPACE: ' ' | '\t';
