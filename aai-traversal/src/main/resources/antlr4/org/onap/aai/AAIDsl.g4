/**
 * Define a grammar called AAIDsl
 */
grammar AAIDsl;


aaiquery: startStatement limitStep?;

startStatement: nodeStep;

traverse: ('>' dslStatement);

dslStatement: (unionQuery | nodeStep);

unionQuery: '[' dslStatement ( ',' (dslStatement))* ']';

nodeStep: node storableStep traverse*;

storableStep: store? propertyFilter* (not? where)?;

propertyFilter: (not? '(' key (',' value)* ')');

where: ('(' traverse+ ')');

limitStep: 'LIMIT' numericValue;

numericValue: NUM;

node: NODE;

key: KEY;

value: KEY | NUM;

store: '*';

not: '!';

fragment ESC: '\\' '\'' ;	

NUM : '0' | [1-9] [0-9]*;

NODE: [a-z0-9_-]+;

KEY: '\'' ( ESC | ~['\r\n])+ '\'';

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
