import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.eee.odlPlugin.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.vodafone.poc.eee.odlPlugin.utils.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The type Odl rest service client impl tester.
 */
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OdlRESTServiceClientImplTester {

    /**
     * Test a configure topology.
     */
    @Test
    public void testA_ConfigureTopology() {

        System.out.println("*** start testA_ConfigureTopology ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        try {
            odlRESTServiceClient.configureTopology(NETWORK_TOPOLOGY_JSON_FILE_NAME_FOR_TEST);
            System.out.println("SUCCESS - topology configured");
            System.out.println("*** stop testA_ConfigureTopology ***");
        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test b is topology already configured.
     */
    @Test
    public void testB_IsTopologyAlreadyConfigured() {

        System.out.println("*** start testB_IsTopologyAlreadyConfigured ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        try {
            assertTrue(odlRESTServiceClient.isTopologyAlreadyConfigured(NETWORK_TOPOLOGY_JSON_FILE_NAME_FOR_TEST));
            System.out.println("SUCCESS - Topology Already Configured");
            System.out.println("*** stop testB_IsTopologyAlreadyConfigured ***");

        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test g get interface list.
     */
    @Test
    public void testG_GetInterfaceList() {

        System.out.println("*** start testG_GetInterfaceList ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        List<InterfaceData> expIfItemList = expectedInterfaceDataList();

        try {
            InterfaceListHandler interfaceListHandler = odlRESTServiceClient.getInterfaceDataList();
            List<InterfaceData> interfaceDataList = interfaceListHandler.getInterfaceDataList();

            for ( InterfaceData expIfItemCursor : expIfItemList ) {

                Boolean found = false;
                InterfaceData ifItem = null;
                InterfaceData expIfItem = expIfItemCursor;

                for ( InterfaceData ifItemCursor : interfaceDataList) {

                    if ( ifItemCursor.getIfRef().equals(expIfItemCursor.getIfRef()) ) {
                        found = true;
                        ifItem = ifItemCursor;
                        break;
                    }
                }

                if (found) {
                    assertEquals(ifItem.getNetworkRef(), expectedNetworkRef());
                    assertEquals(ifItem.getIfMaximumCapacity(), expIfItem.getIfMaximumCapacity());
                    assertEquals(ifItem.getIfCurrentCapacity(), expIfItem.getIfCurrentCapacity());
                    break;
                }
            }

            System.out.println("*** stop testG_GetInterfaceList ***");
        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test h enable interface rate monitoring.
     */
    @Test
    public void testH_EnableInterfaceRateMonitoring() {

        System.out.println("*** start testH_EnableInterfaceRateMonitoring ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();
        //   > networkRefField: "mini-link-topo"
        //   > ifRefField: "mini-link-6691-2:WAN-1/1/1" (Introduces the reference to the termination point housed into ietf-interface instance)
        //   > collectionInterval: "60" (Statistics collection time interval in seconds)
        //   > monitoringEnable: "true"/"false" (Enable/Disable - Flag to indicate monitoring enable state)
        //   > historyLength: "2" (Monitored values history records length)
        try {
            odlRESTServiceClient.enableDisableInterfaceRateMonitoring(
                "mini-link-topo", "mini-link-6691-2:WAN-1/1/2", "10",
                    "true", "2");
            System.out.println("Enabled");
            System.out.println("*** stop testH_EnableInterfaceRateMonitoring ***");
        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test i set interface rate.
     */
    @Test
    public void testI_SetInterfaceCurrentCapacity() {

        System.out.println("*** start testI_SetInterfaceCurrentCapacity ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        List<InterfaceData> interfaceDataList = expectedInterfaceDataList();

        try {
            odlRESTServiceClient.setInterfaceCurrentCapacity("mini-link-topo", "mini-link-6691-2:WAN-1/1/2", 3676483L);
            System.out.println("SUCCESS interface capacity set");
            System.out.println("*** stop testI_SetInterfaceCurrentCapacity ***");
        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test l get interface monitored rate.
     */
    @Test
    public void testL_GetInterfaceMonitoredRate() {

        System.out.println("*** start testL_GetInterfaceMonitoredRate ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        try {
            MonitoredRateHandler monitoredRateHandler = odlRESTServiceClient.getInterfaceMonitoredData("mini-link-topo", "mini-link-6691-2:WAN-1/1/2");
            System.out.println("monitoredRate: " + monitoredRateHandler.getMonitoredRate());
            System.out.println("timeInterval: " + monitoredRateHandler.getTimeInterval());
            System.out.println("bandwidthCapacity: " + monitoredRateHandler.getBandwidthCapacity());
            System.out.println("timeStamp: " + monitoredRateHandler.getTimeStamp());
            System.out.println("currentOutputPower: " + monitoredRateHandler.getCurrentOutputPower());
            System.out.println("nominalOutputPower: " + monitoredRateHandler.getNominalOutputPower());

            System.out.println("*** stop testL_GetInterfaceMonitoredRate ***");
        } catch (HttpURLConnectionFailException | OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test z disable interface rate monitoring.
     */
    @Test
    public void testZ_DisableInterfaceRateMonitoring() {

        System.out.println("*** start testEnableDisableInterfaceRateMonitoring ***");
        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();
        //   > networkRefField: "mini-link-topo"
        //   > ifRefField: "mini-link-6691-2:WAN-1/1/1" (Introduces the reference to the termination point housed into ietf-interface instance)
        //   > collectionInterval: "60" (Statistics collection time interval in seconds)
        //   > monitoringEnable: "true"/"false" (Enable/Disable - Flag to indicate monitoring enable state)
        //   > historyLength: "2" (Monitored values history records length)
        try {
            odlRESTServiceClient.enableDisableInterfaceRateMonitoring(
                    "mini-link-topo", "mini-link-6691-2:WAN-1/1/1", "60",
                    "false", "2");
            System.out.println("Disabled");
            System.out.println("*** stop testEnableDisableInterfaceRateMonitoring ***");
        } catch (HttpURLConnectionFailException e) {
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            e.printStackTrace();
        }
    }

    /**
     * Expected network ref string.
     *
     * @return the string
     */
    public String expectedNetworkRef() {
        return "mini-link-topo";
    }

    /**
     * Expected interface item list list.
     *
     * @return the list
     */
    public List<InterfaceData> expectedInterfaceDataList() {

        List<InterfaceData> interfaceDataList = new ArrayList<InterfaceData>();

        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-2:WAN-1/1/2", 5962375L, 5962375L));
        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-2:WAN-1/1/1", 98338500L, 16315875L));
        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-1:lag-2:WAN-1/1/1", 34962375L, 34962375L));
        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-1:WAN-1/3/2", 24862125L, 24862125L));
        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-1:LAN-1/4/1", 125000000L, 125000000L));
        interfaceDataList.add(new InterfaceData("mini-link-topo", "mini-link-6691-2:lag-2:WAN-1/1/1", 34962375L, 98338500L));

        return interfaceDataList;
    }
}
