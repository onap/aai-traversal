/**
 * Define a grammar called AAIDsl
 */
grammar AAIDsl;


aaiquery: dslStatement;

dslStatement: (singleNodeStep ) (traverseStep )* limitStep*;

unionQueryStep: LBRACKET dslStatement ( COMMA (dslStatement))* RBRACKET;

traverseStep: (TRAVERSE (  singleNodeStep | unionQueryStep));

singleNodeStep: NODE STORE? (filterStep | filterTraverseStep)*;

filterStep: NOT? (LPAREN KEY (COMMA KEY)* RPAREN);
filterTraverseStep: (LPAREN traverseStep* RPAREN);

limitStep: LIMIT NODE;

LIMIT: 'LIMIT';
NODE: ID;

KEY: ['] (ID | ' ')* ['] ;


AND: [&];

STORE: [*];

OR: [|];

TRAVERSE: [>] ;

LPAREN: [(];
	
RPAREN: [)];

COMMA: [,] ;

EQUAL: [=];

LBRACKET: [[];
	
RBRACKET: [\]];

NOT: [!];

VALUE: DIGIT;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT      : [0-9] ;
ID
   : ( LOWERCASE | UPPERCASE | DIGIT) ( LOWERCASE | UPPERCASE | DIGIT | '-' |'.' |'_')*
   ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines


