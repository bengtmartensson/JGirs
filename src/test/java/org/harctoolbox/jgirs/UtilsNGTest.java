/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.harctoolbox.jgirs;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author bengt
 */
public class UtilsNGTest {

    public UtilsNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }



    /**
     * Test of tokenizer method, of class Utils.
     */
    @Test
    public void testTokenizer() {
        System.out.println("tokenizer");
        String s = "com nnn \"Yamaha RX-V1400\"";
        String[] expResult = { "com", "nnn", "Yamaha RX-V1400" };
        String[] result = Utils.tokenizer(s);
        assertEquals(result, expResult);
    }
}
