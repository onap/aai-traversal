/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest.dsl;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.onap.aai.exceptions.AAIException;

/**
 * The Class DslMain.
 */
public class DslQueryProcessorTest {
	 
	  
	@Test
	public void cloudRegion1Test() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String aaiQuery = "cloud-region* !('cloud-owner','coid')('cloud-region-id','crid')  LIMIT 10";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesExcludeByProperty('cloud-owner','coid')"
				+ ".getVerticesByProperty('cloud-region-id','crid').store('x').cap('x').unfold().dedup().limit(10)";

		String query = dslTest.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void cloudRegion_entitlementTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String aaiQuery = "generic-vnf (> vserver > tenant > cloud-region*('cloud-region-id','One')) > entitlement*";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').where("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'vserver','tenant')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".getVerticesByProperty('cloud-region-id','One').store('x'))"
				+ ".createEdgeTraversal(EdgeType.TREE, 'generic-vnf','entitlement').store('x').cap('x').unfold().dedup()";

		String query = dslTest.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void complex_az_fromComplexTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String aaiQuery = "cloud-region('cloud-owner','coid')('cloud-region-id','crid') > [ availability-zone* , complex*]";
		String query = dslTest.parseAaiQuery(aaiQuery);
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region')"
				+ ".getVerticesByProperty('cloud-owner','coid').getVerticesByProperty('cloud-region-id','crid')"
				+ ".union(builder.newInstance().createEdgeTraversal(EdgeType.TREE, 'cloud-region','availability-zone').store('x')"
				+ ",builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'cloud-region','complex').store('x')).cap('x').unfold().dedup()";

		assertEquals(dslQuery, query);
	}
	
	@Test
	public void cloudRegion_fromComplex1Test() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'complex').getVerticesByProperty('country','count-name')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'complex','cloud-region').store('x').cap('x').unfold().dedup()";
		String aaiQuery = "complex('country','count-name') >  cloud-region*";
		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}
		
	@Test
	public void cloudRegion_fromComplex2Test() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'complex').getVerticesByProperty('country','count-name')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'complex','cloud-region').getVerticesByProperty('cloud-region-version','crv')"
				+ ".store('x').cap('x').unfold().dedup()";
		String aaiQuery = "complex('country','count-name') >  cloud-region*('cloud-region-version','crv')";
		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}
	
	@Test
	public void cloudRegion_fromNfTypeTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'image').getVerticesByProperty('application-vendor','F5')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'image','vserver')"
				+ ".where(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'vserver','generic-vnf').getVerticesByProperty('vnf-name','ZALL1MMSC03'))"
				+ ".createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".store('x').cap('x').unfold().dedup()";
		String aaiQuery = "image('application-vendor','F5') > vserver (>generic-vnf('vnf-name','ZALL1MMSC03')) > tenant > cloud-region*";

		String query = dslTest.parseAaiQuery(aaiQuery);
		assertEquals(builderQuery, query);
	}
	  
	@Test
	public void cloudRegion_fromNfTypeVendorVersionTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'image').getVerticesByProperty('application-vendor','vendor')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'image','vserver').where("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'vserver','generic-vnf').getVerticesByProperty('nf-type','nfType')"
				+ ").createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".store('x').cap('x').unfold().dedup()";

		String aaiQuery = "image('application-vendor','vendor') >  vserver( >generic-vnf('nf-type', 'nfType') ) > tenant > cloud-region*";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void cloud_region_fromVnfTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vnfc').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vnfc','vserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').store('x')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region').store('x')"
				+ ".cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf('vnf-id','vnfId')  > vnfc* > vserver* > tenant* > cloud-region*";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}
		
	@Test
	public void cloud_region_sitesTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region')."
				+ "getVerticesByProperty('cloud-owner','co').store('x').createEdgeTraversal(EdgeType.COUSIN, "
				+ "'cloud-region','complex').store('x').cap('x').unfold().dedup()";

		String aaiQuery = "cloud-region*('cloud-owner','co') > complex*";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}
	 
	@Test
	public void complex_fromVnf2Test() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-Id','vnfId').store('x').union("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x')"
				+ ").cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf*('vnf-Id','vnfId') >  " + "[  pserver* > complex*, "
				+ " vserver > pserver* > complex* " + "]";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}
	
	@Test
	public void complex_fromVnfTest() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();
		  
		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-Id','vnfId').store('x').union("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x')"
				+ ").cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf*('vnf-Id','vnfId') >  " + "[  pserver* > complex*, "
				+ " vserver > pserver* > complex* " + "]";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void fn_topology1Test() throws AAIException {
		DslQueryProcessor dslTest = new DslQueryProcessor();

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'business')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'business','customer').getVerticesByProperty('customer-id','a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'customer','service-subscription').getVerticesByProperty('service-subscription-id','Nimbus')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'service-subscription','service-instance').getVerticesByProperty('service-instance-id','sid')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'service-instance','generic-vnf').store('x')"
				+ ".union(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vnfc').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pnf').store('x')).cap('x').unfold().dedup()";

		String aaiQuery = "business > customer('customer-id', 'a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb') > service-subscription('service-subscription-id', 'Nimbus') "
				+ " > service-instance('service-instance-id','sid') > generic-vnf* "
				+ " > [ vnfc* , vserver*, pserver* , pnf* ]";

		String query = dslTest.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

}
