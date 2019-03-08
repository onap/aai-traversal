/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 */
package org.onap.aai.rest.dsl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.contains;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.AAISetup;
import org.onap.aai.exceptions.AAIException;

/**
 * The Class DslMain.
 */
public class DslQueryProcessorTest extends AAISetup {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void cloudRegion1Test() throws AAIException {
		String aaiQuery = "cloud-region* !('cloud-owner','coid')('cloud-region-id','cr id')  LIMIT 10";

		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesExcludeByProperty('cloud-owner','coid')"
				+ ".getVerticesByProperty('cloud-region-id','cr id').store('x').cap('x').unfold().dedup().limit(10)";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void cloudRegion_entitlementTest() throws AAIException {

		String aaiQuery = "generic-vnf('vnf-id','vnfId') (> vserver > tenant > cloud-region*('cloud-region-id','One')) > entitlement*";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').where("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver').createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".getVerticesByProperty('cloud-region-id','One').store('x'))"
				+ ".createEdgeTraversal(EdgeType.TREE, 'generic-vnf','entitlement').store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void complex_az_fromComplexTest() throws AAIException {

		String aaiQuery = "cloud-region('cloud-owner','coid')('cloud-region-id','crid') > [ availability-zone* , complex*]";
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region')"
				+ ".getVerticesByProperty('cloud-owner','coid').getVerticesByProperty('cloud-region-id','crid')"
				+ ".union(builder.newInstance().createEdgeTraversal(EdgeType.TREE, 'cloud-region','availability-zone').store('x')"
				+ ",builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'cloud-region','complex').store('x')).cap('x').unfold().dedup()";

		assertEquals(dslQuery, query);
	}

	@Test
	public void cloudRegion_fromComplex1Test() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'complex').getVerticesByProperty('data-center-code','data-center-code-name')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'complex','cloud-region').store('x').cap('x').unfold().dedup()";
		String aaiQuery = "complex('data-center-code','data-center-code-name') >  cloud-region*";
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void cloudRegion_fromComplex2Test() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'complex').getVerticesByProperty('data-center-code','data-center-code-name')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'complex','cloud-region').getVerticesByProperty('cloud-region-version','crv')"
				+ ".store('x').cap('x').unfold().dedup()";
		String aaiQuery = "complex('data-center-code','data-center-code-name') >  cloud-region*('cloud-region-version','crv')";
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void cloudRegion_fromNfTypeTest() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'image').getVerticesByProperty('application-vendor','F5')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'image','vserver')"
				+ ".where(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'vserver','generic-vnf').getVerticesByProperty('vnf-name','ZALL1MMSC03'))"
				+ ".createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".store('x').cap('x').unfold().dedup()";
		String aaiQuery = "image('application-vendor','F5') > vserver (>generic-vnf('vnf-name','ZALL1MMSC03')) > tenant > cloud-region*";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(builderQuery, query);
	}

	@Test
	public void cloudRegion_fromNfTypeVendorVersionTest() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'image').getVerticesByProperty('application-vendor','vendor')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'image','vserver').where("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'vserver','generic-vnf').getVerticesByProperty('nf-type','nfType')"
				+ ").createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region')"
				+ ".store('x').cap('x').unfold().dedup()";

		String aaiQuery = "image('application-vendor','vendor') >  vserver( >generic-vnf('nf-type', 'nfType') ) > tenant > cloud-region*";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void cloud_region_fromVnfTest() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vnfc').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vnfc','vserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'vserver','tenant').store('x')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'tenant','cloud-region').store('x')"
				+ ".cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf('vnf-id','vnfId')  > vnfc* > vserver* > tenant* > cloud-region*";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void cloud_region_sitesTest() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region')."
				+ "getVerticesByProperty('cloud-owner','co').store('x').createEdgeTraversal(EdgeType.COUSIN, "
				+ "'cloud-region','complex').store('x').cap('x').unfold().dedup()";

		String aaiQuery = "cloud-region*('cloud-owner','co') > complex*";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void complex_fromVnf2Test() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').store('x').union("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x')"
				+ ").cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf*('vnf-id','vnfId') >  " + "[  pserver* > complex*, "
				+ " vserver > pserver* > complex* " + "]";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void complex_fromVnfTest() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').store('x').union("
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','complex').store('x')"
				+ ").cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf*('vnf-id','vnfId') >  " + "[  pserver* > complex*, "
				+ " vserver > pserver* > complex* " + "]";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void fn_topology1Test() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'customer')"
				+ ".getVerticesByProperty('global-customer-id','a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'customer','service-subscription').getVerticesByProperty('service-subscription-id','Nimbus')"
				+ ".createEdgeTraversal(EdgeType.TREE, 'service-subscription','service-instance').getVerticesByProperty('service-instance-id','sid')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'service-instance','generic-vnf').store('x')"
				+ ".union(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vnfc').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').store('x'),"
				+ "builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pnf').store('x')).cap('x').unfold().dedup()";

		String aaiQuery = "customer('global-customer-id', 'a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb') > service-subscription('service-subscription-id', 'Nimbus') "
				+ " > service-instance('service-instance-id','sid') > generic-vnf* "
				+ " > [ vnfc* , vserver*, pserver* , pnf* ]";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void vnf_Dsl() throws AAIException {

		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').where(builder.newInstance().union(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver')"
				+ ".getVerticesByProperty('hostname','hostname1'),builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').getVerticesByProperty('hostname','hostname1'))).store('x').cap('x').unfold().dedup()";

		String aaiQuery = "generic-vnf*('vnf-id','vnfId') (> [pserver('hostname','hostname1'), "
				+ "vserver > pserver('hostname','hostname1')])";
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);

		assertEquals(builderQuery, query);
	}

	@Test
	public void hasPropertyTest() throws AAIException {
		String aaiQuery = "cloud-region* ('cloud-owner')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesByProperty('cloud-owner').store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void hasPropertyValuesTest() throws AAIException {
		String aaiQuery = "cloud-region* ('cloud-owner','cloud-owner1','cloud-owner2')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesByProperty('cloud-owner', new ArrayList<>(Arrays.asList('cloud-owner1','cloud-owner2'))).store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void hasNotPropertyValuesTest() throws AAIException {
		String aaiQuery = "cloud-region* !('cloud-owner','cloud-owner1','cloud-owner2')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesExcludeByProperty('cloud-owner', new ArrayList<>(Arrays.asList('cloud-owner1','cloud-owner2'))).store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void hasNotPropertyNullValuesTest() throws AAIException {
		String aaiQuery = "cloud-region* !('cloud-owner',' ',' null ')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesExcludeByProperty('cloud-owner', new ArrayList<>(Arrays.asList(' ',' null '))).store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}

	@Test
	public void hasPropertyIntegerTest() throws AAIException {
		String aaiQuery = "cloud-region('cloud-owner', 'my-cloud-owner')('cloud-region-id', 'my-region') > vlan-tag*('vlan-id-inner', 20)";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesByProperty('cloud-owner','my-cloud-owner').getVerticesByProperty('cloud-region-id','my-region').createEdgeTraversal(EdgeType.COUSIN, 'cloud-region','vlan-tag').getVerticesByProperty('vlan-id-inner',20).store('x').cap('x').unfold().dedup()";
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}
	
	@Test
	public void hasNotPropertyTest() throws AAIException {
		String aaiQuery = "cloud-region* !('cloud-owner')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesExcludeByProperty('cloud-owner').store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}
	
	@Test
	public void specialCharacterTest() throws AAIException {
		String aaiQuery = "sriov-pf*('pf-pci-id', 'whoa/wow:\"\\'')";
		String dslQuery = "builder.getVerticesByProperty('aai-node-type', 'sriov-pf').getVerticesByProperty('pf-pci-id','whoa/wow:\"\\'').store('x').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(dslQuery, query);
	}
	
	@Test
	public void overlyNestedQueryTest() throws AAIException {
		String aaiQuery = "generic-vnf*('vnf-id','vnfId') (> [pserver('hostname','hostname1'), "
				+ "vserver (> pserver('hostname','hostname1'))]) > vserver";
		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').where(builder.newInstance().union(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver')"
				+ ".getVerticesByProperty('hostname','hostname1'),builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver')"
				+ ".where(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'vserver','pserver').getVerticesByProperty('hostname','hostname1')))).store('x')"
				+ ".createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','vserver').cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(builderQuery, query);
	}
	
	@Test
	public void singleItemUnionTest() throws AAIException {
		String aaiQuery = "generic-vnf*('vnf-id','vnfId') > [pserver('hostname','hostname1')]";
		String builderQuery = "builder.getVerticesByProperty('aai-node-type', 'generic-vnf').getVerticesByProperty('vnf-id','vnfId').store('x')"
				+ ".union(builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf','pserver').getVerticesByProperty('hostname','hostname1'))"
				+ ".cap('x').unfold().dedup()";

		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		assertEquals(builderQuery, query);
	}
	
	@Test
	public void invalidDSLQuerySyntaxTest() throws AAIException {
		String aaiQuery = "wow this is wrong";
		thrown.expect(AAIException.class);
		thrown.expectMessage(contains("Error while processing the query"));
		
		String query = dslQueryProcessor.parseAaiQuery(aaiQuery);
		

	}

}
