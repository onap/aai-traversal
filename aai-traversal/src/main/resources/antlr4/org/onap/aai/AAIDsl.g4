/**
 * Define a grammar called AAIDsl
 */
grammar AAIDsl;


aaiquery: dslStatement limitStep?;

dslStatement: (singleNodeStep );

unionQueryStep: '[' dslStatement ( ',' (dslStatement))* ']';

singleNodeStep: node store? (filterStep | whereStep)* traverseStep*;

traverseStep: ('>' (  singleNodeStep | unionQueryStep));

filterStep: not? ('(' key (',' value)+ ')');

whereStep: ('(' traverseStep+ ')');

limitStep: 'LIMIT' numericValue;

numericValue: NUM;

node: NODE;

key: KEY;

value: KEY | NUM;

store: '*';

not: '!';

fragment ESC: '\\' '\'' ;	

LIMIT: 'LIMIT';

NODE: [a-z_-]+;

KEY: '\'' ( ESC | ~['\r\n])+ '\'';

NUM : '0' | [1-9] [0-9]*;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
