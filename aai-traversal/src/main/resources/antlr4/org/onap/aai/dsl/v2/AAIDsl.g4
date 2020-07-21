/**
 * Define a parser grammar called AAIDsl
 */
grammar AAIDsl;

aaiquery: startStatement limit?;

startStatement: (vertex|unionVertex  ) (traversal)* ;
nestedStatement:  (traversal)+ ;

vertex: label store? (filter)?;

//traversal:  (  vertex|unionVertex edge);
traversal:  (edge* (vertex|unionVertex));

filter:  (selectFilter)* (propertyFilter)* whereFilter*;
propertyFilter: (not? '(' key (',' (key | num | bool))* ')');
selectFilter: ( '{' key (',' key)* '}');
bool: BOOL;

whereFilter: (not? '('  nestedStatement ')' );

//unionVertex: '[' ( nestedStatement ( comma (nestedStatement))*) ']' store?;
unionVertex: '[' ( (edgeFilter)* nestedStatement ( comma ( (edgeFilter)* nestedStatement))*) ']' store?;

comma: ',';
edge: ( TRAVERSE|DIRTRAVERSE) (edgeFilter)?;

edgeFilter: '(' key (',' key )* ')';

num: NUM;
limit: LIMIT num;
label: (ID | NUM )+;
key: KEY;

store: STORE | selectFilter;
not: NOT;

BOOL: 'true'|'TRUE'|'false'|'FALSE';
LIMIT: 'LIMIT'|'limit';
NUM: (DIGIT)+;

/*NODE: (ID | NUM )+;*/
fragment ESCAPED_QUOTE : '\\' '\'';
KEY : '\'' (ESCAPED_QUOTE | ~[\r\n] )*? '\'';

AND: [&];

STORE: [*];

OR: [|];
DIRTRAVERSE: '>>' | '<<' ;

TRAVERSE: '>' ;

EQUAL: [=];

NOT: [!];

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT      : [0-9] ;
fragment  ESC : '\\' . ;
fragment ID_SPECIALS: [-:_];

ID
   :  ( LOWERCASE | UPPERCASE  | DIGIT | ID_SPECIALS)
   ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
