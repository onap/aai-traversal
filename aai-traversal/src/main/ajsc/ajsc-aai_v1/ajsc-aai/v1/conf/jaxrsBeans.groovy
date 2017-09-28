beans{
	xmlns cxf: "http://camel.apache.org/schema/cxf"
	xmlns jaxrs: "http://cxf.apache.org/jaxrs"
	xmlns util: "http://www.springframework.org/schema/util"

	SearchProvider(org.onap.aai.rest.search.SearchProvider)
	ModelAndNamedQueryRestProvider(org.onap.aai.rest.search.ModelAndNamedQueryRestProvider)
	QueryConsumer(org.onap.aai.rest.QueryConsumer)

	V3ThroughV7Retired(org.onap.aai.rest.retired.V3ThroughV7Consumer)

	EchoResponse(org.onap.aai.rest.util.EchoResponse)


	util.list(id: 'jaxrsServices') {

		ref(bean:'SearchProvider')
		ref(bean:'ModelAndNamedQueryRestProvider')
		ref(bean:'QueryConsumer')
		ref(bean:'V3ThroughV7Retired')
		ref(bean:'EchoResponse')
	}
}