package org.onap.aai.rest.search;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class ServiceTopologyTest extends QueryTest {
    public ServiceTopologyTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }
    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex servinst = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "servInstId01", "service-type", "servType01");
        Vertex gv = graph.addVertex(T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "gvId", "vnf-name", "name", "vnf-type", "type");
        Vertex vnfc = graph.addVertex(T.id, "3", "aai-node-type", "vnfc","vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function");

        Vertex vipipv4addresslist1 = graph.addVertex(T.label, "vip-ipv4-address-list", T.id, "4", "aai-node-type", "vip-ipv4-address-list", "vip-ipv4-addres", "vip-ipv4-address1");
        Vertex subnet1 = graph.addVertex(T.label, "subnet", T.id, "5", "aai-node-type", "subnet", "subnet-id", "subnet1");
        Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "6", "aai-node-type", "l3-network", "network-id", "network1-id1", "network-name", "network1-name1");

        Vertex vipipv6addresslist1 = graph.addVertex(T.label, "vip-ipv6-address-list", T.id, "7", "aai-node-type", "vip-ipv6-address-list", "vip-ipv6-address", "vip-ipv6-address2");
        Vertex subnet2 = graph.addVertex(T.label, "subnet", T.id, "8", "aai-node-type", "subnet", "subnet-id", "subnet2");
        Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "9", "aai-node-type", "l3-network", "network-id", "network1-id2", "network-name", "network2-name2");

        Vertex l3inter1ipv4addresslist1 = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "10", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "l3-interface-ipv4-address1");
        Vertex l3network3 = graph.addVertex(T.label, "l3-network", T.id, "11", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network2-name3");
        Vertex subnet3 = graph.addVertex(T.label, "subnet", T.id, "12", "aai-node-type", "subnet", "subnet-id", "subnet3");
        Vertex l3network3_2 = graph.addVertex(T.label, "l3-network", T.id, "13", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network3_2-name3_2");

        Vertex l3inter1ipv6addresslist1 = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "14", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "l3-interface-ipv6-address1");
        Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "15", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network2-name4");
        Vertex subnet4 = graph.addVertex(T.label, "subnet", T.id, "16", "aai-node-type", "subnet", "subnet-id", "subnet4");
        Vertex l3network4_2 = graph.addVertex(T.label, "l3-network", T.id, "17", "aai-node-type", "l3-network", "network-id", "network3-id4", "network-name", "network4_2-name4_2");


        Vertex vserv1 = graph.addVertex(T.id, "18", "aai-node-type", "vserver", "vserver-id", "vservId1", "vserver-name", "vservName1");
        Vertex tenant1 = graph.addVertex(T.id, "19", "aai-node-type", "tenant", "tenant-id", "ten1", "tenant-name", "tenName1");
        Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "20", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id1", "l-interface-name", "l-interface-name1");
                    Vertex l3inter1ipv4addresslist2 = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "21", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "l3-interface-ipv4-address2");
                    Vertex l3network5 = graph.addVertex(T.label, "l3-network", T.id, "22", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network2-name3");
                    Vertex subnet5 = graph.addVertex(T.label, "subnet", T.id, "23", "aai-node-type", "subnet", "subnet-id", "subnet3");
                    Vertex l3network5_2 = graph.addVertex(T.label, "l3-network", T.id, "24", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network3_2-name3_2");

                    Vertex l3inter1ipv6addresslist2 = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "25", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "l3-interface-ipv6-address1");
                    Vertex l3network6 = graph.addVertex(T.label, "l3-network", T.id, "26", "aai-node-type", "l3-network", "network-id", "network3-id3", "network-name", "network2-name4");
                    Vertex subnet6 = graph.addVertex(T.label, "subnet", T.id, "27", "aai-node-type", "subnet", "subnet-id", "subnet4");
                    Vertex l3network6_2 = graph.addVertex(T.label, "l3-network", T.id, "28", "aai-node-type", "l3-network", "network-id", "network3-id4", "network-name", "network4_2-name4_2");

        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "29", "aai-node-type", "pserver", "hostname", "pservername1");


        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "30", "aai-node-type", "pserver", "hostname", "pservername1");
        Vertex pserverint = graph.addVertex(T.label, "p-interface", T.id, "31", "aai-node-type", "p-interface", "interface-name", "xe0/0/0");
        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "32", "aai-node-type", "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, servinst , gv);
        rules.addEdge(g,gv,vnfc);
        rules.addEdge(g,vnfc,vipipv4addresslist1);
        rules.addEdge(g,vipipv4addresslist1,subnet1);
        rules.addTreeEdge(g,subnet1,l3network1);
        rules.addEdge(g,vnfc,vipipv6addresslist1);
        rules.addEdge(g,vipipv6addresslist1,subnet2);
        rules.addTreeEdge(g,subnet2,l3network2);
        rules.addTreeEdge(g,vnfc,l3inter1ipv4addresslist1);
        rules.addEdge(g,l3inter1ipv4addresslist1,l3network3);
        rules.addEdge(g,l3inter1ipv4addresslist1,subnet3);
        rules.addTreeEdge(g,subnet3,l3network3_2);
        rules.addTreeEdge(g,vnfc,l3inter1ipv6addresslist1);
        rules.addEdge(g,l3inter1ipv6addresslist1,l3network4);
        rules.addEdge(g,l3inter1ipv6addresslist1,subnet4);
        rules.addTreeEdge(g,subnet4,l3network4_2);
        rules.addEdge(g,gv,vserv1);
        rules.addTreeEdge(g,vserv1,tenant1);
        rules.addTreeEdge(g,vserv1,linterface1);
        rules.addTreeEdge(g,linterface1,l3inter1ipv4addresslist2);
        rules.addEdge(g,l3inter1ipv4addresslist2,l3network5);
        rules.addEdge(g,l3inter1ipv4addresslist2,subnet5);
        rules.addTreeEdge(g,subnet5,l3network5_2);
        rules.addTreeEdge(g,linterface1,l3inter1ipv6addresslist2);
        rules.addEdge(g,l3inter1ipv6addresslist2,l3network6);
        rules.addEdge(g,l3inter1ipv6addresslist2,subnet6);
        rules.addTreeEdge(g,subnet6,l3network6_2);
        rules.addEdge(g,vserv1,pserver1);
        rules.addEdge(g,gv,pserver2);
        rules.addTreeEdge(g,pserver2,pserverint);
        rules.addEdge(g,pserverint,plink1);

        expectedResult.add(servinst);
        expectedResult.add(gv);
        expectedResult.add(vnfc);
        expectedResult.add(vipipv4addresslist1);
        expectedResult.add(subnet1);
        expectedResult.add(l3network1);
        expectedResult.add(vipipv6addresslist1);
        expectedResult.add(subnet2);
        expectedResult.add(l3network2);
        expectedResult.add(l3inter1ipv4addresslist1);
        expectedResult.add(l3network3);
        expectedResult.add(subnet3);
        expectedResult.add(l3network3_2);
        expectedResult.add(l3inter1ipv6addresslist1);
        expectedResult.add(l3network4);
        expectedResult.add(subnet4);
        expectedResult.add(l3network4_2);
        expectedResult.add(vserv1);
        expectedResult.add(tenant1);
        expectedResult.add(linterface1);
        expectedResult.add(l3inter1ipv4addresslist2);
        expectedResult.add(l3network5);
        expectedResult.add(subnet5);
        expectedResult.add(l3network5_2);
        expectedResult.add(l3inter1ipv6addresslist2);
        expectedResult.add(l3network6);
        expectedResult.add(subnet6);
        expectedResult.add(l3network6_2);
        expectedResult.add(pserver1);
        expectedResult.add(pserver2);
        expectedResult.add(pserverint);
        expectedResult.add(plink1);


    }
    @Override
    protected String getQueryName() {
        return "service-topology";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-instance");
    }
    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
