package org.backmeup.storage.model.utils.test;

import java.io.UnsupportedEncodingException;

import org.backmeup.storage.model.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {
	
	@Test
	public void testGetHexString() {
		byte[] byteArray = { (byte) 255, (byte) 254, (byte) 253, (byte) 252,
				(byte) 251, (byte) 250 };
		
		String expected = "fffefdfcfbfa";
		
		String actual = "";
		try {
			actual = StringUtils.getHexString(byteArray);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(expected, actual);
	}
}
