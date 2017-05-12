beans{
	xmlns cxf: "http://camel.apache.org/schema/cxf"
	xmlns jaxrs: "http://cxf.apache.org/jaxrs"
	xmlns util: "http://www.springframework.org/schema/util"

	SearchProvider(org.openecomp.aai.rest.search.SearchProvider)
	ModelAndNamedQueryRestProvider(org.openecomp.aai.rest.search.ModelAndNamedQueryRestProvider)
	QueryConsumer(org.openecomp.aai.rest.QueryConsumer)

	V3ThroughV7Retired(org.openecomp.aai.rest.retired.V3ThroughV7Consumer)
	V7V8NamedQueries(org.openecomp.aai.rest.retired.V7V8NamedQueries)

	EchoResponse(org.openecomp.aai.rest.util.EchoResponse)


	util.list(id: 'jaxrsServices') {

		ref(bean:'SearchProvider')
		ref(bean:'ModelAndNamedQueryRestProvider')
		ref(bean:'QueryConsumer')
		ref(bean: 'V7V8NamedQueries')
		ref(bean:'V3ThroughV7Retired')

		ref(bean:'EchoResponse')
	}
}