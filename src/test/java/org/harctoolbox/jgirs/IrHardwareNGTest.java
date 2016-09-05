package org.harctoolbox.jgirs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.IrDummy;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class IrHardwareNGTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public IrHardwareNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of singleHardware method, of class IrHardware.
     */
    @Test
    public void testSingleHardware() {
        System.out.println("singleHardware");
        String className = "IrDummy";
        List<String> parameters = new ArrayList<>(2);
        parameters.add(className);

        try {
            List<GirsHardware> result = GirsHardware.singleHardware(parameters);
            GirsHardware hw = result.get(0);
            String version = hw.getHardware().getVersion();
            Assert.assertEquals(IrDummy.defaultVersion, version);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | IOException ex) {
            Logger.getLogger(IrHardwareNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        String silly = "Godzilla";
        parameters.add(silly);
        try {
            List<GirsHardware> result = GirsHardware.singleHardware(parameters);
            GirsHardware hw = result.get(0);
            String version = hw.getHardware().getVersion();
            Assert.assertEquals(silly, version);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | IOException ex) {
            Logger.getLogger(IrHardwareNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
