package com.tencent.cos.xml.exception;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by bradyxiao on 2018/3/14.
 */
@RunWith(AndroidJUnit4.class)
public class CosXmlClientExceptionTest {

    @Test
    public void test() throws Exception{
        CosXmlClientException cosXmlClientException = new CosXmlClientException("exception cause by client");
        assertEquals("exception cause by client", cosXmlClientException.getMessage());

        CosXmlClientException cosXmlClientException2 = new CosXmlClientException("exception cause by client", new NullPointerException());
        assertEquals("exception cause by client", cosXmlClientException2.getMessage());
        assertEquals(true, cosXmlClientException2.getCause() instanceof NullPointerException);
    }
}