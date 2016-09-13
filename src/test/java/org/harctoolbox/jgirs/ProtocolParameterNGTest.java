/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.jgirs;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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
public class ProtocolParameterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private ProtocolParameter nec1_12;
    private ProtocolParameter neC1_12;
    private ProtocolParameter nec1_123;
    private ProtocolParameter mec1_123;
    private ProtocolParameter nec1_alt;
    private ProtocolParameter nec1_124;

    public ProtocolParameterNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        nec1_12 = new ProtocolParameter("nec1", 1, 2);
        neC1_12 = new ProtocolParameter("neC1", 1, 2);
        nec1_123 = new ProtocolParameter("nec1", 1, 2, 3);
        nec1_124 = new ProtocolParameter("nec1", 1, 2, 4);
        mec1_123 = new ProtocolParameter("mec1", 1, 2, 3);
        Map<String, Long> map = new HashMap<>(4);
        map.put("D", 1L);
        map.put("F", 2L);
        nec1_alt = new ProtocolParameter("nec1", map);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of equals method, of class ProtocolParameter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        assertTrue(nec1_12.equals(nec1_12));
        assertTrue(nec1_12.equals(neC1_12));
        assertFalse(nec1_12.equals(nec1_123));
        assertTrue(nec1_12.equals(nec1_alt));
        assertFalse(nec1_123.equals(nec1_124));
    }

    /**
     * Test of hashCode method, of class ProtocolParameter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        ProtocolParameter instance = nec1_123;
        int expResult = 3464295;
        int result = instance.hashCode();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class ProtocolParameter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        String expResult = "nec1 D=1 F=3 S=2";
        String result = nec1_123.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class ProtocolParameter.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        assertEquals(nec1_123.compareTo(mec1_123), 1);
        assertEquals(nec1_12.compareTo(nec1_12), 0);
        assertEquals(nec1_12.compareTo(neC1_12), 0);
        assertEquals(nec1_12.compareTo(nec1_123), -1);
        assertEquals(nec1_12.compareTo(nec1_alt), 0);
        assertEquals(nec1_123.compareTo(nec1_124), -1);
    }

    /**
     * Test of clone method, of class ProtocolParameter.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        ProtocolParameter instance = nec1_123;
        Object result;
        try {
            result = instance.clone();
            assertTrue(instance.equals(result));
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ProtocolParameterNGTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
